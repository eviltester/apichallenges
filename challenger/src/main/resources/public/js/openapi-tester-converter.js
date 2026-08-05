(function (root, factory) {
  'use strict';

  const api = factory();

  if (typeof module === 'object' && module.exports) {
    module.exports = api;
  }

  if (root) {
    root.ApiChallengesOpenApiTesterConverter = api;
  }
}(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  const OPENAPI_METHODS = ['get', 'put', 'post', 'delete', 'options', 'head', 'patch', 'trace'];
  const PRACTICAL_METHODS = ['get', 'post', 'put', 'patch', 'delete', 'options', 'head'];
  const BODY_METHODS = ['post', 'put', 'patch', 'delete'];
  const VALIDATION_KEYS = [
    'enum',
    'const',
    'pattern',
    'minLength',
    'maxLength',
    'minimum',
    'maximum',
    'exclusiveMinimum',
    'exclusiveMaximum',
    'multipleOf',
    'minItems',
    'maxItems',
    'uniqueItems',
    'minProperties',
    'maxProperties',
  ];

  const PATH_ITEM_NON_OPERATION_KEYS = [
    '$ref',
    'summary',
    'description',
    'servers',
    'parameters',
  ];

  const CONVERSION_LIMITATIONS = [
    'Tester conversion supports OpenAPI 3.x documents.',
    'Path item references are left unchanged instead of being resolved in the browser.',
    'Schema relaxation is applied to common component, parameter, header, request body, and operation schemas.',
  ];

  /*
   * TODO: OpenAPI coverage is intentionally partial.
   *
   * This converter is designed to make common OpenAPI 3.x REST specs easier to
   * test from Swagger UI or a REST client. It relaxes the parts most likely to
   * stop exploratory requests:
   *
   * - schemas in components.schemas
   * - components.parameters and operation parameters
   * - components.requestBodies and operation requestBody schemas
   * - components.headers
   * - path-level parameters
   * - missing operations that can be generated on normal path items
   *
   * It is not a full OpenAPI dereferencer, validator, or normalizer. Examples
   * of things that are not converted yet:
   *
   * - A path item like "/users": { "$ref": "#/components/pathItems/Users" }
   *   is left as a $ref, so missing verbs are not added to that referenced path.
   * - An external schema like "$ref": "common.yaml#/components/schemas/User"
   *   is not fetched or rewritten in the browser.
   * - Response-only schemas are not relaxed, e.g. a 200 response schema with
   *   enum, pattern, maxLength, or required may remain unchanged.
   * - Response headers are not fully relaxed, e.g. a header schema with an enum
   *   or pattern may still show those restrictions.
   * - callbacks, webhooks, links, examples, encoding rules, security schemes,
   *   server variables, discriminators, and allOf/oneOf/anyOf semantics are not
   *   fully interpreted or rewritten.
   * - Swagger/OpenAPI 2.0 documents are not converted. They can still be opened
   *   by Swagger UI, but tester conversion is for OpenAPI 3.x.
   *
   * User impact:
   *
   * Someone reading "convert my OpenAPI file" might expect every restriction in
   * the whole document to be removed. That is not what this does today. The
   * output is a useful tester-friendly approximation, but some strict rules may
   * remain in less common sections. Users may need to edit those sections
   * manually, or use a backend converter with reference resolution, when they
   * need a more complete permissive OpenAPI file.
   */
  const TESTER_CONVERSION_PROFILES = {
    original: {
      profile: 'original',
      relaxSchemaConstraints: false,
      removeRequiredProperties: false,
      makeNonPathParametersOptional: false,
      allowAdditionalProperties: false,
      makeRequestBodiesOptional: false,
      addMissingOperations: false,
      addLooseRequestBodiesToGeneratedOperations: false,
      verbs: [],
    },
    practical: {
      profile: 'practical',
      relaxSchemaConstraints: true,
      removeRequiredProperties: true,
      makeNonPathParametersOptional: true,
      allowAdditionalProperties: true,
      makeRequestBodiesOptional: true,
      addMissingOperations: true,
      addLooseRequestBodiesToGeneratedOperations: false,
      verbs: PRACTICAL_METHODS,
    },
    aggressive: {
      profile: 'aggressive',
      relaxSchemaConstraints: true,
      removeRequiredProperties: true,
      makeNonPathParametersOptional: true,
      allowAdditionalProperties: true,
      makeRequestBodiesOptional: true,
      addMissingOperations: true,
      addLooseRequestBodiesToGeneratedOperations: true,
      verbs: OPENAPI_METHODS,
    },
  };

  function cloneJson(value) {
    if (value === undefined) {
      return undefined;
    }

    return JSON.parse(JSON.stringify(value));
  }

  function isObject(value) {
    return value !== null && typeof value === 'object' && !Array.isArray(value);
  }

  function profileOptions(profile) {
    if (profile === 'custom') {
      const customDefaults = cloneJson(TESTER_CONVERSION_PROFILES.practical);
      customDefaults.profile = 'custom';
      return customDefaults;
    }

    return cloneJson(TESTER_CONVERSION_PROFILES[profile] || TESTER_CONVERSION_PROFILES.original);
  }

  function normaliseVerb(verb) {
    const normalised = String(verb || '').trim().toLowerCase();
    return OPENAPI_METHODS.includes(normalised) ? normalised : '';
  }

  function normaliseVerbs(verbs) {
    const seen = new Set();
    const selected = [];

    (verbs || []).forEach(function (verb) {
      const normalised = normaliseVerb(verb);
      if (normalised && !seen.has(normalised)) {
        seen.add(normalised);
        selected.push(normalised);
      }
    });

    return selected;
  }

  function conversionPolicy(options) {
    const requestedProfile = String(options && options.profile ? options.profile : 'original');
    const profile = ['original', 'practical', 'aggressive', 'custom'].includes(requestedProfile)
      ? requestedProfile
      : 'original';
    const policy = Object.assign(profileOptions(profile), options || {});

    policy.profile = profile;
    policy.verbs = normaliseVerbs(policy.verbs);

    if (profile !== 'original' && policy.addMissingOperations && policy.verbs.length === 0) {
      policy.verbs = normaliseVerbs(profile === 'aggressive' ? OPENAPI_METHODS : PRACTICAL_METHODS);
    }

    return policy;
  }

  function normaliseOptions(options) {
    return conversionPolicy(options);
  }

  function createMetrics() {
    return {
      constraintsRemoved: 0,
      requiredPropertiesRemoved: 0,
      additionalPropertiesRelaxed: 0,
      parametersMadeOptional: 0,
      requestBodiesMadeOptional: 0,
      operationsAdded: 0,
    };
  }

  function conversionLimitations() {
    return cloneJson(CONVERSION_LIMITATIONS);
  }

  function isOpenApi3(spec) {
    return isObject(spec) && typeof spec.openapi === 'string' && spec.openapi.indexOf('3.') === 0;
  }

  function convert(spec, options) {
    const policy = conversionPolicy(options);
    const metrics = createMetrics();

    if (policy.profile === 'original') {
      return originalConversionResult(spec, policy, metrics);
    }

    if (!isOpenApi3(spec)) {
      throw new Error('Tester conversion supports OpenAPI 3.x specs. Open the original file in Swagger UI without conversion.');
    }

    const converted = cloneJson(spec);

    relaxComponents(converted, policy, metrics);
    relaxPaths(converted, policy, metrics);

    if (policy.addMissingOperations) {
      addMissingOperations(converted, policy, metrics);
    }

    return testerConversionResult(converted, policy, metrics);
  }

  function originalConversionResult(spec, policy, metrics) {
    return {
      spec: cloneJson(spec),
      converted: false,
      config: policy,
      metrics: metrics,
      limitations: conversionLimitations(),
      summary: 'Original OpenAPI rendered unchanged.',
    };
  }

  function testerConversionResult(spec, policy, metrics) {
    return {
      spec: spec,
      converted: true,
      config: policy,
      metrics: metrics,
      limitations: conversionLimitations(),
      summary: conversionSummary(policy, metrics),
    };
  }

  function relaxComponents(spec, config, metrics) {
    if (!isObject(spec.components)) {
      return;
    }

    Object.values(spec.components.schemas || {}).forEach(function (schema) {
      relaxSchema(schema, config, metrics);
    });

    Object.values(spec.components.parameters || {}).forEach(function (parameter) {
      relaxParameter(parameter, config, metrics);
    });

    Object.values(spec.components.requestBodies || {}).forEach(function (requestBody) {
      relaxRequestBody(requestBody, config, metrics);
    });

    Object.values(spec.components.headers || {}).forEach(function (header) {
      if (isObject(header.schema)) {
        relaxSchema(header.schema, config, metrics);
      }
      relaxContentSchemas(header.content, config, metrics);
    });
  }

  function relaxPaths(spec, config, metrics) {
    if (!isObject(spec.paths)) {
      return;
    }

    Object.values(spec.paths).forEach(function (pathItem) {
      if (!isObject(pathItem) || pathItem.$ref) {
        return;
      }

      (pathItem.parameters || []).forEach(function (parameter) {
        relaxParameter(parameter, config, metrics);
      });

      OPENAPI_METHODS.forEach(function (method) {
        const operation = pathItem[method];
        if (!isObject(operation)) {
          return;
        }

        (operation.parameters || []).forEach(function (parameter) {
          relaxParameter(parameter, config, metrics);
        });

        relaxRequestBody(operation.requestBody, config, metrics);
      });
    });
  }

  function relaxParameter(parameter, config, metrics) {
    if (!isObject(parameter) || parameter.$ref) {
      return;
    }

    if (parameter.in === 'path') {
      parameter.required = true;
    } else if (config.makeNonPathParametersOptional && parameter.required === true) {
      parameter.required = false;
      metrics.parametersMadeOptional += 1;
    }

    if (isObject(parameter.schema)) {
      relaxSchema(parameter.schema, config, metrics);
    }

    relaxContentSchemas(parameter.content, config, metrics);
  }

  function relaxRequestBody(requestBody, config, metrics) {
    if (!isObject(requestBody) || requestBody.$ref) {
      return;
    }

    if (config.makeRequestBodiesOptional && requestBody.required === true) {
      requestBody.required = false;
      metrics.requestBodiesMadeOptional += 1;
    }

    relaxContentSchemas(requestBody.content, config, metrics);
  }

  function relaxContentSchemas(content, config, metrics) {
    if (!isObject(content)) {
      return;
    }

    Object.values(content).forEach(function (mediaType) {
      if (isObject(mediaType) && isObject(mediaType.schema)) {
        relaxSchema(mediaType.schema, config, metrics);
      }
    });
  }

  function relaxSchema(schema, config, metrics, seen) {
    if (!schema || typeof schema !== 'object') {
      return;
    }

    const visited = seen || new WeakSet();

    if (visited.has(schema)) {
      return;
    }
    visited.add(schema);

    if (Array.isArray(schema)) {
      schema.forEach(function (item) {
        relaxSchema(item, config, metrics, visited);
      });
      return;
    }

    if (config.relaxSchemaConstraints) {
      VALIDATION_KEYS.forEach(function (key) {
        if (Object.prototype.hasOwnProperty.call(schema, key)) {
          delete schema[key];
          metrics.constraintsRemoved += 1;
        }
      });
    }

    if (config.removeRequiredProperties && Array.isArray(schema.required)) {
      delete schema.required;
      metrics.requiredPropertiesRemoved += 1;
    }

    if (config.allowAdditionalProperties && schema.additionalProperties === false) {
      schema.additionalProperties = true;
      metrics.additionalPropertiesRelaxed += 1;
    }

    Object.keys(schema).forEach(function (key) {
      if (['example', 'examples', 'default'].includes(key)) {
        return;
      }

      relaxSchema(schema[key], config, metrics, visited);
    });
  }

  function addMissingOperations(spec, config, metrics) {
    if (!isObject(spec.paths)) {
      return;
    }

    const operationIds = collectOperationIds(spec);

    Object.keys(spec.paths).forEach(function (path) {
      const pathItem = spec.paths[path];
      if (!isObject(pathItem) || pathItem.$ref) {
        return;
      }

      const pathParameters = collectPathParameters(pathItem);

      config.verbs.forEach(function (verb) {
        if (PATH_ITEM_NON_OPERATION_KEYS.includes(verb) || pathItem[verb]) {
          return;
        }

        pathItem[verb] = generatedOperation(verb, path, pathParameters, operationIds, config);
        metrics.operationsAdded += 1;
      });
    });
  }

  function collectOperationIds(spec) {
    const operationIds = new Set();

    Object.values(spec.paths || {}).forEach(function (pathItem) {
      if (!isObject(pathItem)) {
        return;
      }

      OPENAPI_METHODS.forEach(function (method) {
        const operation = pathItem[method];
        if (isObject(operation) && operation.operationId) {
          operationIds.add(operation.operationId);
        }
      });
    });

    return operationIds;
  }

  function collectPathParameters(pathItem) {
    const parameters = [];
    const seen = new Set();

    function addParameter(parameter) {
      if (!isObject(parameter)) {
        return;
      }

      const isPathRef = typeof parameter.$ref === 'string' && parameter.$ref.toLowerCase().includes('parameter');
      const key = parameter.$ref || `${parameter.in || ''}:${parameter.name || ''}`;

      if ((parameter.in === 'path' || isPathRef) && !seen.has(key)) {
        seen.add(key);
        parameters.push(cloneJson(parameter));
      }
    }

    (pathItem.parameters || []).forEach(addParameter);

    OPENAPI_METHODS.forEach(function (method) {
      const operation = pathItem[method];
      if (isObject(operation)) {
        (operation.parameters || []).forEach(addParameter);
      }
    });

    return parameters;
  }

  function generatedOperation(verb, path, pathParameters, operationIds, config) {
    const operation = {
      summary: `Generated ${verb.toUpperCase()} tester operation`,
      description: 'Generated by the API Challenges tester OpenAPI converter so this method can be tried from Swagger UI or a REST client. The real API may still reject this request.',
      operationId: uniqueOperationId(verb, path, operationIds),
      responses: {
        '405': {
          description: 'Method not allowed or unsupported by this API.',
        },
        default: {
          description: 'Response returned by the API when this generated tester operation is tried.',
        },
      },
    };

    if (pathParameters.length > 0) {
      operation.parameters = cloneJson(pathParameters);
    }

    if (config.addLooseRequestBodiesToGeneratedOperations && BODY_METHODS.includes(verb)) {
      operation.requestBody = looseRequestBody();
    }

    return operation;
  }

  function looseRequestBody() {
    return {
      required: false,
      content: {
        'application/json': {
          schema: {
            type: 'object',
            additionalProperties: true,
          },
        },
      },
    };
  }

  function uniqueOperationId(verb, path, operationIds) {
    const pathName = path
      .replace(/[{}]/g, '')
      .replace(/[^A-Za-z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '') || 'root';
    const base = `generatedTester_${verb}_${pathName}`;
    let candidate = base;
    let suffix = 2;

    while (operationIds.has(candidate)) {
      candidate = `${base}_${suffix}`;
      suffix += 1;
    }

    operationIds.add(candidate);
    return candidate;
  }

  function conversionSummary(config, metrics) {
    const profile = config.profile.charAt(0).toUpperCase() + config.profile.slice(1);
    return `${profile} tester OpenAPI generated: added ${metrics.operationsAdded} operations, removed ${metrics.constraintsRemoved} schema constraints, made ${metrics.parametersMadeOptional} parameters optional, and made ${metrics.requestBodiesMadeOptional} request bodies optional.`;
  }

  function stringify(spec) {
    return JSON.stringify(spec, null, 2);
  }

  function convertedFilename(sourceName) {
    const base = String(sourceName || 'openapi')
      .split(/[?#]/)[0]
      .split('/')
      .filter(Boolean)
      .pop() || 'openapi';
    const clean = base
      .replace(/\.(json|yaml|yml)$/i, '')
      .replace(/[^A-Za-z0-9._-]+/g, '-')
      .replace(/^-+|-+$/g, '') || 'openapi';

    return `${clean}-tester-openapi.json`;
  }

  return {
    methods: cloneJson(OPENAPI_METHODS),
    practicalMethods: cloneJson(PRACTICAL_METHODS),
    profileOptions: profileOptions,
    conversionPolicy: conversionPolicy,
    normaliseOptions: normaliseOptions,
    isOpenApi3: isOpenApi3,
    conversionLimitations: conversionLimitations,
    convert: convert,
    stringify: stringify,
    convertedFilename: convertedFilename,
  };
}));
