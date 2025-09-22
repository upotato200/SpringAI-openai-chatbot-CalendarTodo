// 요소 가져오기
const sheet = document.getElementById('chatbotSheet');
const fab = document.getElementById('chatbotFab');
const closeBtn = document.getElementById('cbCloseBtn');
const inputEl = document.getElementById('cbInput');
const sendBtn = document.getElementById('cbSendBtn');
const bodyEl = document.getElementById('cbBody');

// 접근성: 팝업 열릴 때 포커스 관리
let lastActive = null;

// 대화 히스토리(서버에 전달할 형식)
const chatHistory = [];

function detectApiOrigin() {
  try {
    const port = window.location.port;
    if (window.location.protocol === 'file:' || port === '63342') return 'http://localhost:8080';
    return '';
  } catch (e) {
    return 'http://localhost:8080';
  }
}
const CHATBOT_API_ORIGIN = detectApiOrigin();

function resolveApiPath(path) {
  if (path.startsWith('/') && CHATBOT_API_ORIGIN) return CHATBOT_API_ORIGIN + path;
  return path;
}

function openSheet() {
  lastActive = document.activeElement;
  sheet.hidden = false;
  // 애니메이션 상태 플래그
  sheet.setAttribute('data-open', 'true');

  // 첫 포커스는 입력창으로
  requestAnimationFrame(() => inputEl?.focus());
}

function closeSheet() {
  // 애니메이션 자연스럽게: 패널만 축소/페이드
  sheet.removeAttribute('data-open');
  // 애니메이션 시간 후 완전 숨김
  setTimeout(() => {
    sheet.hidden = true;
    // 이전 포커스로 복귀
    lastActive?.focus?.();
  }, 220);
}

function handleBackdrop(e) {
  if (e.target === sheet) closeSheet();
}

// ESC 닫기
function onKeydown(e) {
  if (e.key === 'Escape' && !sheet.hidden) closeSheet();
}

function makeUserBubble(text) {
  const wrap = document.createElement('div');
  wrap.className = 'cb-msg cb-msg--user';

  const bubble = document.createElement('div');
  bubble.className = 'cb-msg__bubble';
  bubble.textContent = text;

  const time = document.createElement('span');
  time.className = 'cb-time';
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  time.textContent = `${hh}:${mm}`;

  wrap.appendChild(bubble);
  wrap.appendChild(time);
  return wrap;
}

function makeAssistantBubblePlaceholder() {
  const wrap = document.createElement('div');
  wrap.className = 'cb-msg cb-msg--ai';

  const bubble = document.createElement('div');
  bubble.className = 'cb-msg__bubble';
  bubble.textContent = '응답 준비 중...';

  const time = document.createElement('span');
  time.className = 'cb-time';
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  time.textContent = `${hh}:${mm}`;

  wrap.appendChild(bubble);
  wrap.appendChild(time);
  return { wrap, bubble };
}

function scrollBodyToBottom() {
  if (!bodyEl) return;
  bodyEl.scrollTop = bodyEl.scrollHeight;
}

async function sendChat() {
  const v = (inputEl?.value || '').trim();
  if (!v) return;
  if (!bodyEl) return;

  // UI: 사용자 메시지 추가
  const userWrap = makeUserBubble(v);
  bodyEl.appendChild(userWrap);
  scrollBodyToBottom();

  // 히스토리에 추가
  chatHistory.push({ role: 'user', content: v, timestamp: Date.now() });
  // 서버로 보낼 히스토리는 최근 n개로 자르기 (필요 시 조정)
  const historyToSend = chatHistory.slice(-20);

  // 준비된 assistant placeholder
  const { wrap: aiWrap, bubble: aiBubble } = makeAssistantBubblePlaceholder();
  bodyEl.appendChild(aiWrap);
  scrollBodyToBottom();

  // 비활성화
  inputEl.disabled = true;
  sendBtn.disabled = true;

  try {
    const res = await fetch(resolveApiPath('/api/chat/message'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Accept': 'application/json; charset=utf-8'
      },
      body: JSON.stringify({ message: v, history: historyToSend })
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('Chat API error', res.status, text);
      aiBubble.textContent = '죄송합니다. 응답을 가져오지 못했습니다.';
      chatHistory.push({ role: 'assistant', content: aiBubble.textContent, timestamp: Date.now() });
      return;
    }

    const json = await res.json();
    console.log('Chat response:', json); // 디버깅용 로그
    const reply = json?.message ?? '응답이 비었습니다.';
    const success = json?.success ?? true;
    
    if (!success) {
      console.warn('Chat response marked as failed:', json);
    }
    
    aiBubble.textContent = reply;

    // 히스토리 추가(assistant)
    chatHistory.push({ role: 'assistant', content: reply, timestamp: json?.timestamp || Date.now() });

    // 스크롤
    scrollBodyToBottom();
  } catch (err) {
    console.error('Network error while chatting', err);
    aiBubble.textContent = '네트워크 오류가 발생했습니다. 다시 시도해 주세요.';
    chatHistory.push({ role: 'assistant', content: aiBubble.textContent, timestamp: Date.now() });
  } finally {
    inputEl.disabled = false;
    sendBtn.disabled = false;
    inputEl.value = '';
    inputEl.focus();
  }
}

// 이벤트 바인딩
fab?.addEventListener('click', openSheet);
closeBtn?.addEventListener('click', closeSheet);
sheet?.addEventListener('click', handleBackdrop);
document.addEventListener('keydown', onKeydown);

// 전송 버튼
sendBtn?.addEventListener('click', sendChat);
inputEl?.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    e.preventDefault();
    sendChat();
  }
});

// 초기 히스토리: 환영 메시지 (선택적)
(function initChatbot() {
  if (!bodyEl) return;
  const welcome = '안녕하세요! 캘린더 & 할 일 도우미입니다. 오늘 일정 요약이나 할 일 관리를 도와드릴게요.';
  const { wrap, bubble } = makeAssistantBubblePlaceholder();
  bubble.textContent = welcome;
  bodyEl.appendChild(wrap);
  chatHistory.push({ role: 'assistant', content: welcome, timestamp: Date.now() });
  scrollBodyToBottom();
})();
