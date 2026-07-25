FROM node:22-alpine

WORKDIR /app
COPY catalog ./catalog
COPY implementations/node-native-http ./implementations/node-native-http

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["node", "implementations/node-native-http/server.js"]
