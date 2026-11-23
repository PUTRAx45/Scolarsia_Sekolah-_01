const fs = require('fs');
let content = fs.readFileSync('server.js', 'utf8');

content = content.replace("app.get('/health', (req, res) => {", "app.get('/health', async (req, res) => {");

fs.writeFileSync('server.js', content, 'utf8');
console.log('PATCH 3 APPLIED');