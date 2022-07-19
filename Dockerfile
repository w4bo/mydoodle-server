FROM node:16-alpine as build
RUN apk update && apk upgrade && apk add --no-cache bash git openssh
RUN mkdir /app
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
RUN npm run build
RUN npm install
EXPOSE 3000
CMD ["node", "web-server.js"]
