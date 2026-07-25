FROM python:3.12-alpine

WORKDIR /app
COPY catalog ./catalog
COPY implementations/python-flask ./implementations/python-flask

WORKDIR /app/implementations/python-flask
RUN pip install --no-cache-dir -r requirements.txt

ENV PORT=3001
ENV FROMHELL_CATALOG=/app/catalog/fromhell-catalog.json
EXPOSE 3001

CMD ["python", "server.py"]
