const fs = require('fs');
let content = fs.readFileSync('server.js', 'utf8');

// 1. Add warmup function
if (!content.includes('async function warmUpOllama()')) {
  content = content.replace(
    /app\.use\('\/api\/', limiter\);/,
    `app.use('/api/', limiter);

// Warm-up Ollama Model
async function warmUpOllama() {
  const isOllama = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim() === 'ollama';
  const shouldWarmup = process.env.WARMUP_OLLAMA !== '0';

  if (isOllama && shouldWarmup) {
    const baseUrl = (process.env.OLLAMA_BASE_URL || 'http://127.0.0.1:11434/v1/chat/completions').trim();
    const model = (process.env.OLLAMA_MODEL || 'llama3.2:latest').trim();
    const keepAlive = (process.env.OLLAMA_KEEP_ALIVE || '30m').trim();

    console.log(\`[Warmup] Memanaskan model Ollama \${model} di latar belakang...\`);
    const start = Date.now();
    try {
      await axios.post(baseUrl, {
        model: model,
        messages: [{ role: 'user', content: 'hello' }],
        max_tokens: 1,
        keep_alive: keepAlive
      }, { timeout: 120000 });
      const duration = Date.now() - start;
      console.log(\`[Warmup] Ollama selesai dipanaskan (\${duration}ms).\`);
    } catch (e) {
      const duration = Date.now() - start;
      console.error(\`[Warmup] Ollama gagal dipanaskan setelah \${duration}ms:\`, e.message);
    }
  }
}
warmUpOllama();
`
  );
}

// 2. Replace checkAiReachable and callAiProvider
const checkReachableMatch = content.match(/\/\/ Lightweight check for AI provider reachability[\s\S]*?(?=\/\/ Helper Call OpenRouter API)/);
if (checkReachableMatch) {
  content = content.replace(checkReachableMatch[0],
`// Lightweight check for AI provider reachability (2s timeout, no token consumption)
async function checkAiReachable() {
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  if (provider === 'ollama') {
    const baseUrl = (process.env.OLLAMA_BASE_URL || 'http://127.0.0.1:11434/v1/chat/completions').trim();
    const rootUrl = baseUrl.substring(0, baseUrl.indexOf('/v1'));
    try {
      const res = await axios.get(rootUrl + '/api/tags', { timeout: 2000 });
      return res.status === 200 ? 'OK (Ollama Local UP)' : \`HTTP \${res.status}\`;
    } catch (e) {
      return \`OFFLINE (Ollama Local: \${e.message})\`;
    }
  } else {
    const apiKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
    if (isKeyPlaceholder(apiKey)) return 'UNCONFIGURED (Key Placeholder)';
    return 'OK (OpenRouter Cloud Configured)';
  }
}

// Unified Call AI Provider (Ollama Primary with OpenRouter Fallback)
async function callAiProvider(messages, temperature = 0.3, maxTokens = 300, responseFormat = null) {
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  const timeoutMs = Number(process.env.AI_TIMEOUT_MS) || 90000;
  const fallback = (process.env.AI_FALLBACK || 'openrouter').toLowerCase().trim();

  if (provider === 'ollama') {
    const baseUrl = (process.env.OLLAMA_BASE_URL || 'http://127.0.0.1:11434/v1/chat/completions').trim();
    const model = (process.env.OLLAMA_MODEL || 'llama3.2:latest').trim();
    const keepAlive = (process.env.OLLAMA_KEEP_ALIVE || '30m').trim();

    const payload = {
      model: model,
      messages: messages,
      temperature: temperature,
      max_tokens: maxTokens,
      keep_alive: keepAlive
    };
    if (responseFormat) payload.response_format = responseFormat;

    const startTime = Date.now();
    try {
      console.log(\`[AI Provider: Ollama] Sending prompt to \${baseUrl} (\${model})...\`);
      const response = await axios.post(baseUrl, payload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: timeoutMs
      });
      const duration = Date.now() - startTime;
      console.log(\`[AI Provider: Ollama] Success (\${duration}ms) - Model: \${model}\`);
      return { data: response.data, duration, provider: 'ollama' };
    } catch (error) {
      const duration = Date.now() - startTime;
      console.error(\`[AI Provider: Ollama Error] Failed after \${duration}ms:\`, error.message);

      const apiKey = (process.env.OPENROUTER_API_KEY || process.env.AI_API_KEY || '').trim();
      const hasValidKey = !isKeyPlaceholder(apiKey);

      if (fallback === 'openrouter' && hasValidKey) {
        console.log('🔄 [AI Fallback] Falling back to OpenRouter Cloud API...');
        try {
          return await callOpenRouter(messages, temperature, maxTokens, responseFormat);
        } catch (fallbackErr) {
          throw fallbackErr;
        }
      }

      if (error.code === 'ECONNREFUSED' || error.message.includes('ECONNREFUSED')) {
        throw { statusCode: 502, code: 'AI_UNREACHABLE', publicMessage: \`Ollama tidak berjalan di \${baseUrl}\` };
      } else if (error.code === 'ETIMEDOUT' || error.message.includes('timeout')) {
        throw { statusCode: 503, code: 'AI_TIMEOUT', publicMessage: 'Koneksi ke server AI melebihi batas waktu (Timeout).' };
      }
      throw { statusCode: 503, code: 'AI_UNAVAILABLE', publicMessage: 'Layanan AI saat ini tidak dapat dijangkau.' };
    }
  } else {
    return await callOpenRouter(messages, temperature, maxTokens, responseFormat);
  }
}

`);
}

// 3. System Prompt
const promptMatch = content.match(/Jawab dalam Bahasa Indonesia, singkat, ramah, 2-3 kalimat, berdasarkan data nilai yang diberikan\./);
if (promptMatch) {
  content = content.replace(promptMatch[0], 'Jawaban Bahasa Indonesia, maksimal 3 kalimat, hanya berdasarkan data nilai yang diberikan, tanpa mengarang angka.');
}

// 4. Update /health/detail
const healthMatch = content.match(/services: {[\s\S]*?},[\s\S]*?environment: {/);
if (healthMatch) {
  content = content.replace(healthMatch[0], `aiProvider: provider,
    aiModel: model,
    aiReachable: aiReachable,
    services: {
      backend: 'UP',
      firebaseAuthEmulator: authStatus,
      firestoreEmulator: firestoreStatus
    },
    environment: {`);

  // Add provider, model, aiReachable before it is sent
  content = content.replace(/const pkg = require\('\.\/package\.json'\);/, `const pkg = require('./package.json');
  const aiReachable = await checkAiReachable();
  const provider = (process.env.AI_PROVIDER || 'ollama').toLowerCase().trim();
  const model = provider === 'ollama' ? (process.env.OLLAMA_MODEL || 'llama3.2:latest') : (process.env.OPENROUTER_MODEL || 'openai/gpt-4o-mini');`)
}

fs.writeFileSync('server.js', content, 'utf8');
console.log('PATCH APPLIED');
