(function (root, factory) {
  'use strict';

  const api = factory();

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }

  if (root) {
    root.ApiChallengesOpenApiParameterPlacement = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  const FIXED_OPERATION_METHODS = [
    'get',
    'put',
    'post',
    'delete',
    'options',
    'head',
    'patch',
    'trace',
    'query',
  ];
  const VALID_SCOPES = ['path', 'all'];
  const VALID_TARGETS = ['keep', 'path', 'operation'];

  function cloneJson(value) {
    if (value === undefined) {
      return undefined;
    }

    return JSON.parse(JSON.stringify(value));
  }

  function isObject(value) {
    return value !== null && typeof value === 'object' && !Array.isArray(value);
  }

  function asArray(value) {
    return Array.isArray(value) ? value : [];
  }

  function isOpenApi3(spec) {
    return isObject(spec) && typeof spec.openapi === 'string' && spec.openapi.indexOf('3.') === 0;
  }

  function normaliseOptions(options) {
    const requestedScope = String(options && options.scope ? options.scope : 'path');
    const requestedTarget = String(options && options.target ? options.target : 'keep');

    return {
      scope: VALID_SCOPES.includes(requestedScope) ? requestedScope : 'path',
      target: VALID_TARGETS.includes(requestedTarget) ? requestedTarget : 'keep',
    };
  }

  function operationEntries(pathItem) {
    const entries = [];

    FIXED_OPERATION_METHODS.forEach(function (method) {
      if (isObject(pathItem[method])) {
        entries.push({
          method: method.toUpperCase(),
          operation: pathItem[method],
        });
      }
    });

    if (isObject(pathItem.additionalOperations)) {
      Object.keys(pathItem.additionalOperations).forEach(function (method) {
        const operation = pathItem.additionalOperations[method];
        if (isObject(operation)) {
          entries.push({
            method: method,
            operation: operation,
          });
        }
      });
    }

    return entries;
  }

  function resolveJsonPointer(document, pointer) {
    if (typeof pointer !== 'string' || !pointer.startsWith('#/')) {
      return null;
    }

    let parts;
    try {
      parts = pointer.substring(2).split('/').map(function (part) {
        return decodeURIComponent(part).replace(/~1/g, '/').replace(/~0/g, '~');
      });
    } catch (error) {
      return null;
    }
    let current = document;

    for (const part of parts) {
      if (!isObject(current) && !Array.isArray(current)) {
        return null;
      }
      if (!Object.prototype.hasOwnProperty.call(current, part)) {
        return null;
      }
      current = current[part];
    }

    return isObject(current) ? current : null;
  }

  function parameterInfo(spec, parameter) {
    if (!isObject(parameter)) {
      return {
        comparable: null,
        key: '',
        location: '',
        reference: '',
        unresolved: true,
      };
    }

    const reference = typeof parameter.$ref === 'string' ? parameter.$ref : '';
    const resolved = reference ? resolveJsonPointer(spec, reference) : parameter;
    const comparable = resolved || parameter;
    const name = typeof comparable.name === 'string' ? comparable.name : '';
    const location = typeof comparable.in === 'string' ? comparable.in : '';
    const key = name && location ? `${location}\u0000${name}` : (reference ? `$ref\u0000${reference}` : '');

    return {
      comparable: comparable,
      key: key,
      location: location,
      reference: reference,
      unresolved: Boolean(reference && !resolved),
    };
  }

  function parameterInScope(info, scope) {
    if (scope === 'all') {
      return true;
    }
    return info.location === 'path';
  }

  function sortedValue(value) {
    if (Array.isArray(value)) {
      return value.map(sortedValue);
    }

    if (!isObject(value)) {
      return value;
    }

    const sorted = {};
    Object.keys(value).sort().forEach(function (key) {
      sorted[key] = sortedValue(value[key]);
    });
    return sorted;
  }

  function canonicalJson(value) {
    return JSON.stringify(sortedValue(value));
  }

  function comparableParameter(spec, parameter) {
    const info = parameterInfo(spec, parameter);
    if (info.reference && !info.unresolved && Object.keys(parameter).length === 1) {
      return info.comparable;
    }
    return parameter;
  }

  function parameterSignature(spec, parameter) {
    return canonicalJson(comparableParameter(spec, parameter));
  }

  function equivalentParameter(spec, first, second) {
    return parameterSignature(spec, first) === parameterSignature(spec, second);
  }

  function warningCollector() {
    const seen = new Set();
    const warnings = [];

    return {
      add(message) {
        if (!seen.has(message)) {
          seen.add(message);
          warnings.push(message);
        }
      },
      values() {
        return warnings.slice();
      },
    };
  }

  function createEvaluationMetrics() {
    return {
      pathItemsInspected: 0,
      operationsInspected: 0,
      sharedDeclarations: 0,
      operationDeclarations: 0,
      pathsWithSharedDeclarations: 0,
      pathsWithOperationDeclarations: 0,
      overrides: 0,
      duplicateDeclarations: 0,
      missingPathParameters: 0,
      optionalPathParameters: 0,
      referencedPathItemsSkipped: 0,
      unresolvedParameters: 0,
    };
  }

  function selectedParameterEntries(spec, parameters, scope, warnings, locationLabel, metrics) {
    const selected = [];
    const seenKeys = new Set();

    asArray(parameters).forEach(function (parameter, index) {
      const info = parameterInfo(spec, parameter);

      if (info.unresolved) {
        metrics.unresolvedParameters += 1;
        warnings.add(`${locationLabel} contains an unresolved parameter reference: ${info.reference}.`);
      }

      if (!parameterInScope(info, scope)) {
        return;
      }

      if (!info.key) {
        warnings.add(`${locationLabel} contains a parameter without a usable name and location.`);
      } else if (seenKeys.has(info.key)) {
        metrics.duplicateDeclarations += 1;
        warnings.add(`${locationLabel} declares the same parameter more than once.`);
      } else {
        seenKeys.add(info.key);
      }

      if (info.location === 'path' && info.comparable.required !== true) {
        metrics.optionalPathParameters += 1;
        warnings.add(`${locationLabel} contains a path parameter that is not marked required.`);
      }

      selected.push({
        index: index,
        info: info,
        parameter: parameter,
      });
    });

    return selected;
  }

  function templateParameterNames(path) {
    const names = [];
    const pattern = /\{([^{}]+)\}/g;
    let match = pattern.exec(path);

    while (match) {
      if (!names.includes(match[1])) {
        names.push(match[1]);
      }
      match = pattern.exec(path);
    }

    return names;
  }

  function pathParameterKeys(entries) {
    const keys = new Set();
    entries.forEach(function (entry) {
      if (entry.info.location === 'path' && entry.info.key) {
        keys.add(entry.info.key);
      }
    });
    return keys;
  }

  function evaluate(spec, options) {
    if (!isOpenApi3(spec)) {
      throw new Error('Path parameter placement supports OpenAPI 3.x specifications.');
    }

    const policy = normaliseOptions(options);
    const metrics = createEvaluationMetrics();
    const warnings = warningCollector();

    Object.keys(spec.paths || {}).forEach(function (path) {
      const pathItem = spec.paths[path];
      if (!isObject(pathItem)) {
        return;
      }

      if (pathItem.$ref) {
        metrics.referencedPathItemsSkipped += 1;
        warnings.add(`${path} uses a referenced Path Item and was not evaluated.`);
        return;
      }

      metrics.pathItemsInspected += 1;
      const operations = operationEntries(pathItem);
      metrics.operationsInspected += operations.length;
      const shared = selectedParameterEntries(
        spec,
        pathItem.parameters,
        policy.scope,
        warnings,
        `${path} Path Item`,
        metrics,
      );
      const sharedKeys = new Set(shared.map(function (entry) {
        return entry.info.key;
      }).filter(Boolean));

      metrics.sharedDeclarations += shared.length;
      if (shared.length > 0) {
        metrics.pathsWithSharedDeclarations += 1;
      }

      let pathHasOperationDeclarations = false;
      const sharedPathKeys = pathParameterKeys(shared);
      const requiredTemplateNames = templateParameterNames(path);

      operations.forEach(function (operationEntry) {
        const operationLabel = `${operationEntry.method} ${path}`;
        const operationParameters = selectedParameterEntries(
          spec,
          operationEntry.operation.parameters,
          policy.scope,
          warnings,
          operationLabel,
          metrics,
        );

        metrics.operationDeclarations += operationParameters.length;
        pathHasOperationDeclarations = pathHasOperationDeclarations || operationParameters.length > 0;

        operationParameters.forEach(function (entry) {
          if (entry.info.key && sharedKeys.has(entry.info.key)) {
            metrics.overrides += 1;
          }
        });

        if (requiredTemplateNames.length > 0) {
          const effectiveKeys = new Set(sharedPathKeys);
          pathParameterKeys(operationParameters).forEach(function (key) {
            effectiveKeys.add(key);
          });

          requiredTemplateNames.forEach(function (name) {
            if (!effectiveKeys.has(`path\u0000${name}`)) {
              metrics.missingPathParameters += 1;
              warnings.add(`${operationLabel} does not declare the {${name}} path parameter.`);
            }
          });
        }
      });

      if (pathHasOperationDeclarations) {
        metrics.pathsWithOperationDeclarations += 1;
      }
    });

    const placement = placementFromMetrics(metrics);

    return {
      placement: placement,
      scope: policy.scope,
      metrics: metrics,
      warnings: warnings.values(),
      summary: evaluationSummary(placement, metrics, warnings.values().length, policy.scope),
    };
  }

  function placementFromMetrics(metrics) {
    if (metrics.sharedDeclarations > 0 && metrics.operationDeclarations > 0) {
      return 'mixed';
    }
    if (metrics.sharedDeclarations > 0) {
      return 'path';
    }
    if (metrics.operationDeclarations > 0) {
      return 'operation';
    }
    return 'none';
  }

  function placementLabel(placement) {
    return {
      path: 'Shared path-level',
      operation: 'Operation-level',
      mixed: 'Mixed',
      none: 'No matching parameters',
    }[placement] || 'Unknown';
  }

  function evaluationSummary(placement, metrics, warningCount, scope) {
    const warningText = warningCount === 1 ? '1 warning' : `${warningCount} warnings`;

    if (placement === 'none' && scope === 'all') {
      return `No Path Item or operation parameters found; there is nothing to move. ${warningText}.`;
    }

    if (placement === 'none') {
      return `No URL path parameters found; there is nothing to move for this scope. ${warningText}. Choose All Path Item parameters to include query, header, and cookie parameters.`;
    }

    return `${placementLabel(placement)}: ${metrics.sharedDeclarations} shared and ${metrics.operationDeclarations} operation-level declarations; ${warningText}.`;
  }

  function createConversionMetrics() {
    return {
      sharedDeclarationsMoved: 0,
      operationCopiesAdded: 0,
      operationDeclarationsPromoted: 0,
      operationDeclarationsRemoved: 0,
      overridesPreserved: 0,
      unsafeDeclarationsSkipped: 0,
    };
  }

  function existingParameterKeys(spec, operation) {
    const keys = new Set();
    asArray(operation.parameters).forEach(function (parameter) {
      const info = parameterInfo(spec, parameter);
      if (info.key) {
        keys.add(info.key);
      }
    });
    return keys;
  }

  function unresolvedMoveIsSafe(spec, info, operations) {
    if (!info.unresolved) {
      return true;
    }

    return operations.every(function (operationEntry) {
      const parameters = asArray(operationEntry.operation.parameters);
      if (parameters.length === 0) {
        return true;
      }

      return parameters.some(function (parameter) {
        return parameterInfo(spec, parameter).key === info.key;
      });
    });
  }

  function moveToOperations(spec, scope, metrics, warnings) {
    Object.keys(spec.paths || {}).forEach(function (path) {
      const pathItem = spec.paths[path];
      if (!isObject(pathItem) || pathItem.$ref) {
        return;
      }

      const operations = operationEntries(pathItem);
      if (operations.length === 0) {
        if (asArray(pathItem.parameters).length > 0) {
          warnings.add(`${path} has no operations, so its shared parameters were retained.`);
        }
        return;
      }

      const movedIndexes = new Set();
      const movedKeys = new Set();
      const parameters = asArray(pathItem.parameters);
      const operationStates = operations.map(function (operationEntry) {
        return {
          copies: [],
          entry: operationEntry,
          keys: existingParameterKeys(spec, operationEntry.operation),
        };
      });

      parameters.forEach(function (parameter, index) {
        const info = parameterInfo(spec, parameter);
        if (!parameterInScope(info, scope)) {
          return;
        }
        if (!info.key) {
          metrics.unsafeDeclarationsSkipped += 1;
          warnings.add(`${path} contains a parameter without a usable name and location.`);
          return;
        }
        if (movedKeys.has(info.key)) {
          metrics.unsafeDeclarationsSkipped += 1;
          warnings.add(`${path} Path Item declares the same parameter more than once.`);
          return;
        }
        if (!unresolvedMoveIsSafe(spec, info, operations)) {
          metrics.unsafeDeclarationsSkipped += 1;
          warnings.add(`${path} contains an external parameter reference that cannot be compared safely with operation parameters.`);
          return;
        }

        operationStates.forEach(function (state) {
          if (state.keys.has(info.key)) {
            metrics.overridesPreserved += 1;
            return;
          }

          state.copies.push(cloneJson(parameter));
          state.keys.add(info.key);
          metrics.operationCopiesAdded += 1;
        });

        movedIndexes.add(index);
        movedKeys.add(info.key);
        metrics.sharedDeclarationsMoved += 1;
      });

      operationStates.forEach(function (state) {
        if (state.copies.length > 0) {
          state.entry.operation.parameters = state.copies.concat(
            asArray(state.entry.operation.parameters),
          );
        }
      });

      if (movedIndexes.size > 0) {
        const retained = parameters.filter(function (parameter, index) {
          return !movedIndexes.has(index);
        });
        if (retained.length > 0) {
          pathItem.parameters = retained;
        } else {
          delete pathItem.parameters;
        }
      }
    });
  }

  function removeOperationParameterIndexes(operation, indexes) {
    if (indexes.size === 0) {
      return;
    }

    const retained = asArray(operation.parameters).filter(function (parameter, index) {
      return !indexes.has(index);
    });
    if (retained.length > 0) {
      operation.parameters = retained;
    } else {
      delete operation.parameters;
    }
  }

  function selectedOperationParameters(spec, operation, scope) {
    return asArray(operation.parameters).map(function (parameter, index) {
      return {
        index: index,
        info: parameterInfo(spec, parameter),
        parameter: parameter,
      };
    }).filter(function (entry) {
      return parameterInScope(entry.info, scope);
    });
  }

  function promoteExistingSharedParameters(spec, path, pathItem, operations, scope, metrics, warnings) {
    const sharedEntries = asArray(pathItem.parameters).map(function (parameter, index) {
      return {
        index: index,
        info: parameterInfo(spec, parameter),
        parameter: parameter,
      };
    }).filter(function (entry) {
      return parameterInScope(entry.info, scope);
    });
    const sharedByKey = new Map();

    sharedEntries.forEach(function (entry) {
      if (!entry.info.key || sharedByKey.has(entry.info.key)) {
        if (entry.info.key) {
          warnings.add(`${path} Path Item declares the same parameter more than once.`);
        }
        return;
      }
      sharedByKey.set(entry.info.key, entry.parameter);
    });

    operations.forEach(function (operationEntry) {
      const remove = new Set();
      selectedOperationParameters(spec, operationEntry.operation, scope).forEach(function (entry) {
        const shared = sharedByKey.get(entry.info.key);
        if (!shared) {
          return;
        }
        if (equivalentParameter(spec, shared, entry.parameter)) {
          remove.add(entry.index);
          metrics.operationDeclarationsRemoved += 1;
        } else {
          metrics.overridesPreserved += 1;
        }
      });
      removeOperationParameterIndexes(operationEntry.operation, remove);
    });

    return sharedByKey;
  }

  function operationCandidates(spec, operations, scope, existingShared, path, warnings) {
    const candidates = new Map();
    const unsafeKeys = new Set();

    operations.forEach(function (operationEntry, operationIndex) {
      const seen = new Set();
      selectedOperationParameters(spec, operationEntry.operation, scope).forEach(function (entry) {
        const key = entry.info.key;
        if (!key || existingShared.has(key)) {
          return;
        }
        if (seen.has(key)) {
          unsafeKeys.add(key);
          warnings.add(`${operationEntry.method} ${path} declares the same parameter more than once.`);
          return;
        }
        seen.add(key);

        if (!candidates.has(key)) {
          candidates.set(key, []);
        }
        candidates.get(key).push({
          index: entry.index,
          operation: operationEntry.operation,
          operationIndex: operationIndex,
          parameter: entry.parameter,
          signature: parameterSignature(spec, entry.parameter),
        });
      });
    });

    unsafeKeys.forEach(function (key) {
      candidates.delete(key);
    });
    return candidates;
  }

  function promotableGroup(occurrences, operationCount) {
    if (occurrences.length !== operationCount) {
      return null;
    }

    const groups = new Map();
    occurrences.forEach(function (occurrence) {
      if (!groups.has(occurrence.signature)) {
        groups.set(occurrence.signature, []);
      }
      groups.get(occurrence.signature).push(occurrence);
    });

    if (operationCount === 1) {
      return occurrences;
    }

    let largest = [];
    let largestIsTied = false;
    groups.forEach(function (group) {
      if (group.length > largest.length) {
        largest = group;
        largestIsTied = false;
      } else if (group.length === largest.length) {
        largestIsTied = true;
      }
    });

    if (largest.length < 2 || largestIsTied) {
      return null;
    }
    return largest;
  }

  function moveToPathItems(spec, scope, metrics, warnings) {
    Object.keys(spec.paths || {}).forEach(function (path) {
      const pathItem = spec.paths[path];
      if (!isObject(pathItem) || pathItem.$ref) {
        return;
      }

      const operations = operationEntries(pathItem);
      if (operations.length === 0) {
        return;
      }

      const existingShared = promoteExistingSharedParameters(
        spec,
        path,
        pathItem,
        operations,
        scope,
        metrics,
        warnings,
      );
      const candidates = operationCandidates(
        spec,
        operations,
        scope,
        existingShared,
        path,
        warnings,
      );

      candidates.forEach(function (occurrences) {
        if (occurrences.length !== operations.length) {
          metrics.unsafeDeclarationsSkipped += 1;
          warnings.add(`${path} cannot share a parameter that is missing from one or more operations.`);
          return;
        }

        const selectedGroup = promotableGroup(occurrences, operations.length);
        if (!selectedGroup) {
          metrics.unsafeDeclarationsSkipped += 1;
          warnings.add(`${path} has conflicting operation parameter definitions with no unambiguous shared definition.`);
          return;
        }

        const promoted = cloneJson(selectedGroup[0].parameter);
        pathItem.parameters = asArray(pathItem.parameters).concat([promoted]);
        metrics.operationDeclarationsPromoted += 1;

        const removals = new Map();
        selectedGroup.forEach(function (occurrence) {
          if (!removals.has(occurrence.operation)) {
            removals.set(occurrence.operation, new Set());
          }
          removals.get(occurrence.operation).add(occurrence.index);
          metrics.operationDeclarationsRemoved += 1;
        });
        removals.forEach(function (indexes, operation) {
          removeOperationParameterIndexes(operation, indexes);
        });

        metrics.overridesPreserved += occurrences.length - selectedGroup.length;
      });
    });
  }

  function convert(spec, options) {
    const policy = normaliseOptions(options);
    const before = evaluate(spec, policy);
    const converted = cloneJson(spec);
    const metrics = createConversionMetrics();
    const warnings = warningCollector();

    if (policy.target === 'operation') {
      moveToOperations(converted, policy.scope, metrics, warnings);
    } else if (policy.target === 'path') {
      moveToPathItems(converted, policy.scope, metrics, warnings);
    }

    const after = evaluate(converted, policy);
    const allWarnings = warningCollector();
    warnings.values().concat(after.warnings).forEach(function (warning) {
      allWarnings.add(warning);
    });
    const changed = canonicalJson(spec) !== canonicalJson(converted);

    return {
      spec: converted,
      changed: changed,
      config: policy,
      before: before,
      after: after,
      metrics: metrics,
      warnings: allWarnings.values(),
      summary: conversionSummary(policy, metrics, after, changed),
    };
  }

  function conversionSummary(policy, metrics, after, changed) {
    if (policy.target === 'keep' || !changed) {
      return `No parameter placement changes were needed. ${after.summary}`;
    }

    if (policy.target === 'operation') {
      return `Converted toward operation-level placement: moved ${metrics.sharedDeclarationsMoved} shared declarations and added ${metrics.operationCopiesAdded} operation copies. ${after.summary}`;
    }

    return `Converted toward shared path-level placement: promoted ${metrics.operationDeclarationsPromoted} declarations and removed ${metrics.operationDeclarationsRemoved} repeated operation declarations. ${after.summary}`;
  }

  return {
    scopes: cloneJson(VALID_SCOPES),
    targets: cloneJson(VALID_TARGETS),
    normaliseOptions: normaliseOptions,
    isOpenApi3: isOpenApi3,
    evaluate: evaluate,
    convert: convert,
  };
}));
