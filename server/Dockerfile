FROM node:20-alpine
WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev
COPY . .
ENV PORT=8080 DATA_FILE=/data/data.json
EXPOSE 8080
CMD ["node","server.js"]
