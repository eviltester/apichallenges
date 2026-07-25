FROM node:22-alpine

WORKDIR /app
COPY catalog ./catalog
COPY implementations/node-express ./implementations/node-express

WORKDIR /app/implementations/node-express
RUN npm install --omit=dev

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["node", "server.js"]
