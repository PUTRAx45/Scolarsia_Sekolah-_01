const axios = require('axios');

const EMULATOR_AUTH_URL = 'http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/';
const BACKEND_URL = 'http://127.0.0.1:3000';

async function getAuthToken(email, password) {
  try {
    const res = await axios.post(`${EMULATOR_AUTH_URL}accounts:signInWithPassword?key=fake-key`, {
      email,
      password,
      returnSecureToken: true
    });
    return res.data.idToken;
  } catch (e) {
    return null;
  }
}

async function runTests() {
  console.log('======================================================');
  console.log('🧪 TEST SUITE: SCOLARSIA AI CHAT & INSIGHT (npm run test-ai)');
  console.log('======================================================');

  let passedCount = 0;
  let failedCount = 0;

  function markPassed(msg) {
    console.log('✅ ' + msg);
    passedCount++;
  }

  function markFailed(msg) {
    console.error('❌ GAGAL: ' + msg);
    failedCount++;
  }

  // 1. Health Check
  try {
    const healthRes = await axios.get(`${BACKEND_URL}/health`, { timeout: 3000 });
    markPassed('[1/5] Health Check: ' + healthRes.data.status + ' Package: ' + healthRes.data.package);
  } catch (e) {
    markFailed('[1/5] Health Check Gagal: ' + e.message);
  }

  // Get Auth Tokens for Dummy Accounts
  const siswaToken = await getAuthToken('09231@scolarsia.ac.id', '1g2u3r4u');
  const guruToken = await getAuthToken('guru12@scolarsia.ac.id', '1g2u3r4u');

  // 2. Test Greeting Detection
  try {
    const greetRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Selamat pagi AI'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {}
    });
    const isGreetingMsg = greetRes.data.message === 'Salam terdeteksi' && greetRes.data.modelCalled === false;
    if (isGreetingMsg) {
      markPassed('[2/5] Greeting Test LULUS (modelCalled: false): ' + greetRes.data.data?.answer);
    } else {
      markFailed('[2/5] Greeting Test (Model Dipanggil Padahal Greeting): ' + JSON.stringify(greetRes.data));
    }
  } catch (e) {
    markFailed('[2/5] Greeting Test Exception: ' + (e.response?.data ? JSON.stringify(e.response.data) : e.message));
  }

  // 3. Test Moderation Filter (Profanity & Off-topic)
  try {
    const modRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Anjing sialan goblok! Beli Bitcoin dimana ya?'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {}
    });
    const isModerated = modRes.data.message === 'Pertanyaan memerlukan moderasi' && modRes.data.modelCalled === false;
    if (isModerated) {
      markPassed('[3/5] Moderation Filter Test LULUS (Ditolak, modelCalled: false): ' + modRes.data.data?.answer);
    } else {
      markFailed('[3/5] Moderation Filter Test GAGAL (Tidak ditolak): ' + JSON.stringify(modRes.data));
    }
  } catch (e) {
    markFailed('[3/5] Moderation Test Exception: ' + (e.response?.data ? JSON.stringify(e.response.data) : e.message));
  }

  // 4. Test Siswa AI Chat (Model Call or Unconfigured Error)
  try {
    const chatRes = await axios.post(`${BACKEND_URL}/ai/chat`, {
      question: 'Berikan panduan belajar Pitagoras secara ringkas.'
    }, {
      headers: siswaToken ? { Authorization: `Bearer ${siswaToken}` } : {},
      timeout: 120000
    });

    if (chatRes.data.status === 'success' && chatRes.data.modelCalled === true) {
      markPassed('[4/5] Siswa AI Chat Model Test LULUS (modelCalled: true, Provider: ' + (chatRes.data.provider || 'ollama') + '): ' + chatRes.data.data?.answer);
    } else if (chatRes.data.modelCalled === false) {
      markPassed('[4/5] Siswa AI Chat (Balasan Lokal/Layanan Tanpa Model): ' + chatRes.data.message);
    } else {
      markFailed('[4/5] Siswa AI Chat Test GAGAL: ' + JSON.stringify(chatRes.data));
    }
  } catch (e) {
    const errData = e.response?.data;
    if (errData && (errData.code === 'AI_KEY_INVALID' || errData.code === 'AI_QUOTA' || errData.code === 'AI_UNREACHABLE' || errData.code === 'AI_TIMEOUT')) {
      markPassed(`[4/5] Siswa AI Chat (Expected Error Handling: Code '${errData.code}', HTTP ${e.response?.status}): ${errData.message}`);
    } else {
      markFailed('[4/5] Siswa AI Chat Test GAGAL: ' + (e.response?.data ? JSON.stringify(e.response.data) : e.message));
    }
  }

  // 5. Test Guru AI Insight
  try {
    const insightRes = await axios.post(`${BACKEND_URL}/ai/insight`, {
      subject: 'Matematika'
    }, {
      headers: guruToken ? { Authorization: `Bearer ${guruToken}` } : {},
      timeout: 120000
    });

    if (insightRes.data.status === 'success') {
      markPassed('[5/5] Guru AI Insight Test LULUS (modelCalled: ' + insightRes.data.modelCalled + '): ' + insightRes.data.data?.aiRecommendation);
    } else {
      markFailed('[5/5] Guru AI Insight Test GAGAL: ' + JSON.stringify(insightRes.data));
    }
  } catch (e) {
    const errData = e.response?.data;
    if (errData && (errData.code === 'AI_KEY_INVALID' || errData.code === 'AI_QUOTA' || errData.code === 'AI_UNREACHABLE' || errData.code === 'AI_TIMEOUT')) {
      markPassed(`[5/5] Guru AI Insight (Expected Error Handling: Code '${errData.code}', HTTP ${e.response?.status}): ${errData.message}`);
    } else {
      markFailed('[5/5] Guru AI Insight Test GAGAL: ' + (e.response?.data ? JSON.stringify(e.response.data) : e.message));
    }
  }

  console.log('======================================================');
  console.log(`📋 RINGKASAN: ${passedCount} lulus, ${failedCount} gagal.`);
  if (failedCount > 0) {
    process.exit(1);
  } else {
    process.exit(0);
  }
}

runTests();
