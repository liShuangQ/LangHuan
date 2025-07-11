FROM library/node:20-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
EXPOSE 9088
CMD ["npm", "run", "dev"]