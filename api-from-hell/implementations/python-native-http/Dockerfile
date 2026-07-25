FROM python:3.12-alpine

WORKDIR /app
COPY catalog ./catalog
COPY implementations/python-native-http ./implementations/python-native-http

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["python", "implementations/python-native-http/server.py"]
