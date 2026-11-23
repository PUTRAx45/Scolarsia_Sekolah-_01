const fs = require('fs');
let content = fs.readFileSync('server.js', 'utf8');

content = content.replace(/const pkg = require\('\.\/package\.json'\);\s*return res\.status\(isHealthy \? 200 : 503\)\.json\({/, `const pkg = require('./package.json');
  const aiReachable = await checkAiReachable();
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  const model = provider === 'ollama' ? (process.env.OLLAMA_MODEL || 'llama3.2:latest') : (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini');

  return res.status(isHealthy ? 200 : 503).json({`);

fs.writeFileSync('server.js', content, 'utf8');
console.log('PATCH 4 APPLIED');
