const fs = require('fs');
let content = fs.readFileSync('server.js', 'utf8');

content = content.replace(`// 4. Endpoint Lightweight GET /health (Ultra-fast response for connection tests)
app.get('/health', (req, res) => {
  const pkg = require('./package.json');
  const aiReachable = await checkAiReachable();
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  const model = provider === 'ollama' ? (process.env.OLLAMA_MODEL || 'llama3.2:latest') : (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini');
  return res.json({`, `// 4. Endpoint Lightweight GET /health (Ultra-fast response for connection tests)
app.get('/health', (req, res) => {
  const pkg = require('./package.json');
  return res.json({`);

content = content.replace(`const authStatus = await checkAuthPromise;
  const firestoreStatus = await checkFirestorePromise;

  const isHealthy = authStatus === 'OK' && firestoreStatus === 'OK';
  const pkg = require('./package.json');

  return res.status(isHealthy ? 200 : 503).json({`, `const authStatus = await checkAuthPromise;
  const firestoreStatus = await checkFirestorePromise;

  const isHealthy = authStatus === 'OK' && firestoreStatus === 'OK';
  const pkg = require('./package.json');
  const aiReachable = await checkAiReachable();
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  const model = provider === 'ollama' ? (process.env.OLLAMA_MODEL || 'llama3.2:latest') : (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini');

  return res.status(isHealthy ? 200 : 503).json({`);

fs.writeFileSync('server.js', content, 'utf8');
console.log('PATCH 2 APPLIED');
