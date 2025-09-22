// ===== Utilities =====
const pad = n => String(n).padStart(2, '0');
const ymd = d => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
const fromYMD = (s) => { const [y,m,dd] = s.split('-').map(Number); return new Date(y, m-1, dd); };

// ===== LocalStorage Keys =====
const STORAGE_KEY = 'plannerData.v1';
const THEME_KEY = 'theme';

// ===== Store helpers =====
function loadStore() {
  try {
    const data = localStorage.getItem(STORAGE_KEY);
    return data ? JSON.parse(data) : {};
  } catch (e) {
    console.warn('Failed to load from localStorage', e);
    return {};
  }
}

function saveStore() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.store));
  } catch (e) {
    console.warn('Failed to save to localStorage', e);
  }
}

// 서버 기반으로 동기화를 수행합니다. 로컬 저장은 더 이상 주 저장소가 아닙니다.
function detectApiOrigin() {
  try {
    const port = window.location.port;
    if (window.location.protocol === 'file:' || port === '63342') return 'http://localhost:8080';
    // If the page is served from the same origin as backend, use relative paths
    return '';
  } catch (e) {
    return 'http://localhost:8080';
  }
}

const API_ORIGIN = detectApiOrigin();
const API_BASE = API_ORIGIN ? `${API_ORIGIN}/api/todos` : '/api/todos';

async function apiFetch(path, opts = {}) {
  try {
    const url = path.startsWith('/') && API_ORIGIN ? API_ORIGIN + path : path;
    const res = await fetch(url, opts);
    if (!res.ok) {
      const text = await res.text().catch(() => '');
      console.error('API error', path, res.status, text);
      throw new Error(`API ${path} failed: ${res.status}`);
    }
    if (res.status === 204) return null;
    return await res.json();
  } catch (e) {
    console.error('Network/API fetch failed', path, e);
    throw e;
  }
}

async function fetchTodosByDate(date) {
  return await apiFetch(`${API_BASE}?date=${encodeURIComponent(date)}`) || [];
}

async function fetchTodosRange(from, to) {
  return await apiFetch(`${API_BASE}/range?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`) || [];
}

async function createTodoOnServer(text, date) {
  return await apiFetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, date })
  });
}

async function updateTodoOnServer(id, text, done) {
  return await apiFetch(`${API_BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, done })
  });
}

async function deleteTodoOnServer(id) {
  return await apiFetch(`${API_BASE}/${id}`, { method: 'DELETE' });
}

// 월별 데이터를 서버에서 불러와 state.store에 채웁니다.
async function loadMonthData(year, month) {
  const first = `${year}-${pad(month+1)}-01`;
  const lastDay = new Date(year, month+1, 0).getDate();
  const last = `${year}-${pad(month+1)}-${pad(lastDay)}`;
  try {
    const items = await fetchTodosRange(first, last);
    // 해당 월 범위의 기존 데이터 초기화
    for (let d = 1; d <= lastDay; d++) {
      const key = `${year}-${pad(month+1)}-${pad(d)}`;
      delete state.store[key];
    }
    items.forEach(t => {
      if (!t.date) return;
      state.store[t.date] = state.store[t.date] || [];
      state.store[t.date].push({ id: t.id, text: t.text, done: !!t.done });
    });
  } catch (e) {
    console.warn('Failed to load month data', e);
  }
}

// ===== App State =====
const state = {
  current: new Date(),   // 현재 보고 있는 달(1일 기준)
  selected: new Date(),  // 선택된 날짜
  store: loadStore(),
};

// ===== DOM =====
const calTitle = document.getElementById('calTitle');
const monthGrid = document.getElementById('monthGrid');
const monthMeta = document.getElementById('monthMeta');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const todayBtn = document.getElementById('todayBtn');

const selectedDateLabel = document.getElementById('selectedDateLabel');
const todoStats = document.getElementById('todoStats');
const todoInput = document.getElementById('todoInput');
const addBtn = document.getElementById('addBtn');
const todoList = document.getElementById('todoList');
const emptyState = document.getElementById('emptyState');
const clearDoneBtn = document.getElementById('clearDoneBtn');
const clearAllBtn = document.getElementById('clearAllBtn');

// 테마 토글 버튼
const themeToggle = document.getElementById('themeToggle');

// ===== Theme (class .dark 전환) =====
function getTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  if (saved === 'dark' || saved === 'light') return saved;
  return document.documentElement.classList.contains('dark') ? 'dark' : 'light';
}

function applyTheme(mode) {
  document.documentElement.classList.toggle('dark', mode === 'dark');
  localStorage.setItem(THEME_KEY, mode);
  updateThemeToggleLabel(mode);
}

function updateThemeToggleLabel(mode = getTheme()) {
  if (!themeToggle) return;
  if (mode === 'dark') {
    themeToggle.textContent = '라이트 모드';
    themeToggle.setAttribute('aria-label', '라이트 모드로 전환');
    themeToggle.title = '라이트 모드로 전환';
  } else {
    themeToggle.textContent = '다크 모드';
    themeToggle.setAttribute('aria-label', '다크 모드로 전환');
    themeToggle.title = '다크 모드로 전환';
  }
}

// ===== Calendar Render =====
function renderCalendar() {
  const y = state.current.getFullYear();
  const m = state.current.getMonth();
  calTitle.textContent = `${y}년 ${m+1}월`;

  // 이번 달 통계
  const prefix = `${y}-${pad(m+1)}-`;
  const keys = Object.keys(state.store).filter(k => k.startsWith(prefix));
  const counts = keys.reduce((acc, k) => {
    const arr = state.store[k] || [];
    const done = arr.filter(t => t.done).length;
    const open = arr.length - done;
    acc.open += open; acc.done += done; return acc;
  }, { open: 0, done: 0 });
  if (monthMeta) monthMeta.textContent = `${counts.open} 미완료 · ${counts.done} 완료`;

  monthGrid.innerHTML = '';

  // 달력 셀 계산 (6주 고정)
  const first = new Date(y, m, 1);
  const startDay = first.getDay();             
  const daysInMonth = new Date(y, m+1, 0).getDate();
  const prevDays = new Date(y, m, 0).getDate();

  const cells = [];
  for (let i = 0; i < startDay; i++) {
    const d = prevDays - startDay + 1 + i;
    cells.push({ date: new Date(y, m-1, d), out: true });
  }
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push({ date: new Date(y, m, d), out: false });
  }
  const trailing = 42 - cells.length;
  for (let i = 1; i <= trailing; i++) {
    cells.push({ date: new Date(y, m+1, i), out: true });
  }

  const todayStr = ymd(new Date());
  const selStr = ymd(state.selected);

  for (const c of cells) {
    const el = document.createElement('div');
    el.className = 'day' + (c.out ? ' out' : '');
    const ds = ymd(c.date);
    if (ds === todayStr) el.classList.add('today');
    if (ds === selStr) el.classList.add('selected');
    el.setAttribute('role', 'gridcell');
    el.setAttribute('aria-label', ds);
    el.innerHTML = `<div class="date-badge">${c.date.getDate()}</div>`;

    // dots 표시
    const arr = state.store[ds] || [];
    if (arr.length) {
      const open = arr.filter(t => !t.done).length;
      const done = arr.length - open;
      const dots = document.createElement('div');
      dots.className = 'dots';
      const dot = (cls) => { const d = document.createElement('span'); d.className = 'dot-task ' + cls; return d; };
      if (open) dots.appendChild(dot(''));
      if (done) dots.appendChild(dot('dot-done'));
      el.appendChild(dots);
    }

    el.addEventListener('click', () => selectDate(c.date));
    monthGrid.appendChild(el);
  }
}

// ===== To-Do Logic =====
function getTodos(dateStr) { return state.store[dateStr] || []; }
function setTodos(dateStr, arr) {
  state.store[dateStr] = arr;
  saveStore(); // 로컬 백업
  renderTodos();
  renderCalendar();
}

async function renderTodos() {
  const key = ymd(state.selected);
  if (selectedDateLabel) selectedDateLabel.textContent = key;
  // 서버에 항목이 없으면 해당 날짜를 서버에서 불러옵니다.
  if (!state.store[key]) {
    try {
      const items = await fetchTodosByDate(key);
      state.store[key] = items.map(t => ({ id: t.id, text: t.text, done: !!t.done }));
    } catch (e) {
      state.store[key] = [];
    }
  }
  const arr = getTodos(key);
  todoList.innerHTML = '';
  if (emptyState) emptyState.hidden = arr.length !== 0;

  const open = arr.filter(t => !t.done).length;
  if (todoStats) todoStats.textContent = `${arr.length}개 · ${open} 남음`;

  arr.forEach(item => {
    const row = document.createElement('div');
    row.className = 'todo-item' + (item.done ? ' done' : '');
    row.dataset.id = item.id;
    row.innerHTML = `
      <input type="checkbox" ${item.done ? 'checked' : ''} aria-label="완료 표시" />
      <div class="todo-text-wrap">
        <span class="todo-text">${item.text}</span>
        <input class="todo-edit-input form-control" type="text" value="${item.text}" style="display:none;" />
      </div>
      <div class="todo-actions">
        <button class="ghost-btn edit-btn" title="수정">✏️</button>
        <button class="ghost-btn save-btn" title="저장" style="display:none;">💾</button>
        <button class="ghost-btn cancel-btn" title="취소" style="display:none;">❌</button>
        <button class="ghost-btn delete-btn" title="삭제">🗑️</button>
      </div>
    `;

    const textSpan = row.querySelector('.todo-text');
    const editInput = row.querySelector('.todo-edit-input');
    const editBtn = row.querySelector('.edit-btn');
    const saveBtn = row.querySelector('.save-btn');
    const cancelBtn = row.querySelector('.cancel-btn');
    const deleteBtn = row.querySelector('.delete-btn');

    // 완료 토글
    row.querySelector('input[type="checkbox"]').addEventListener('change', async (e) => {
      const newDone = e.target.checked;
      try {
        const res = await updateTodoOnServer(item.id, item.text, newDone);
        item.done = !!res.done;
      } catch (err) {
        // 실패하면 체크 상태 되돌리기
        e.target.checked = !newDone;
        console.warn('Failed to update done state', err);
      }
      row.classList.toggle('done', item.done);
      setTodos(key, [...arr]);
    });

    // 수정 모드 진입
    editBtn.addEventListener('click', () => {
      textSpan.style.display = 'none';
      editInput.style.display = '';
      editBtn.style.display = 'none';
      saveBtn.style.display = '';
      cancelBtn.style.display = '';
      editInput.focus();
    });

    // 저장
    saveBtn.addEventListener('click', async () => {
      const val = editInput.value.trim();
      if (!val) return;
      try {
        const res = await updateTodoOnServer(item.id, val, item.done);
        item.text = res.text;
        textSpan.textContent = res.text;
        setTodos(key, [...arr]);
      } catch (err) {
        console.warn('Failed to save edit', err);
      }
      textSpan.style.display = '';
      editInput.style.display = 'none';
      editBtn.style.display = '';
      saveBtn.style.display = 'none';
      cancelBtn.style.display = 'none';
    });

    // 취소
    cancelBtn.addEventListener('click', () => {
      editInput.value = item.text;
      textSpan.style.display = '';
      editInput.style.display = 'none';
      editBtn.style.display = '';
      saveBtn.style.display = 'none';
      cancelBtn.style.display = 'none';
    });

    // 삭제
    deleteBtn.addEventListener('click', async () => {
      const result = await Swal.fire({
        title: '할 일 삭제',
        text: '정말로 이 할 일을 삭제하시겠어요?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
      });

      if (!result.isConfirmed) return;

      try {
        await deleteTodoOnServer(item.id);
        const next = arr.filter(t => String(t.id) !== String(item.id));
        setTodos(key, next);

        Swal.fire({
          title: '삭제 완료',
          text: '할 일이 삭제되었습니다.',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false
        });
      } catch (err) {
        console.warn('Failed to delete', err);
        Swal.fire({
          title: '삭제 실패',
          text: '할 일 삭제 중 오류가 발생했습니다.',
          icon: 'error'
        });
      }
    });

    todoList.appendChild(row);
  });
}

async function addTodo(text) {
  const key = ymd(state.selected);
  try {
    const created = await createTodoOnServer(text, key);
    const arr = getTodos(key);
    const newItem = { id: created.id, text: created.text, done: !!created.done };
    setTodos(key, [newItem, ...arr]);
    return true;
  } catch (err) {
    console.warn('Failed to create todo', err);
    Swal.fire({
      title: '추가 실패',
      text: '할 일 추가에 실패했습니다.',
      icon: 'error'
    });
    return false;
  }
}

function selectDate(d) {
  state.selected = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  state.current = new Date(state.selected.getFullYear(), state.selected.getMonth(), 1);
  renderCalendar();
  renderTodos();
}

// ===== Navigation =====
if (prevBtn) prevBtn.addEventListener('click', async () => {
  state.current = new Date(state.current.getFullYear(), state.current.getMonth()-1, 1);
  await loadMonthData(state.current.getFullYear(), state.current.getMonth());
  renderCalendar();
});
if (nextBtn) nextBtn.addEventListener('click', async () => {
  state.current = new Date(state.current.getFullYear(), state.current.getMonth()+1, 1);
  await loadMonthData(state.current.getFullYear(), state.current.getMonth());
  renderCalendar();
});
if (todayBtn) todayBtn.addEventListener('click', async () => { selectDate(new Date()); await loadMonthData(state.current.getFullYear(), state.current.getMonth()); });

// ===== To-Do Buttons =====
if (addBtn) addBtn.addEventListener('click', async () => {
  const v = (todoInput?.value || '').trim();
  if (!v) return;
  const success = await addTodo(v);
  if (success) {
    todoInput.value = '';
    todoInput.focus();
  }
});
if (todoInput) todoInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') { addBtn?.click(); }
});
if (clearDoneBtn) {
  clearDoneBtn.addEventListener('click', async () => {
    const key = ymd(state.selected);
    const arr = getTodos(key);
    const doneItems = arr.filter(t => t.done);
    if (doneItems.length === 0) {
      Swal.fire({
        title: '알림',
        text: '완료된 할 일이 없습니다.',
        icon: 'info'
      });
      return;
    }

    const result = await Swal.fire({
      title: '완료된 할 일 삭제',
      text: `완료된 ${doneItems.length}개 항목을 삭제하시겠습니까?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: '삭제',
      cancelButtonText: '취소'
    });

    if (!result.isConfirmed) return;
    for (const it of doneItems) {
      try {
        await deleteTodoOnServer(it.id);
      } catch (e) {
        console.warn('Failed to delete done item', it, e);
      }
    }
    // reload day from server
    try {
      const fresh = await fetchTodosByDate(key);
      state.store[key] = fresh.map(t => ({ id: t.id, text: t.text, done: !!t.done }));

      Swal.fire({
        title: '삭제 완료',
        text: `완료된 ${doneItems.length}개 항목이 삭제되었습니다.`,
        icon: 'success',
        timer: 1500,
        showConfirmButton: false
      });
    } catch (e) {
      state.store[key] = arr.filter(t => !t.done);
      Swal.fire({
        title: '삭제 실패',
        text: '일부 항목 삭제 중 오류가 발생했습니다.',
        icon: 'error'
      });
    }
    setTodos(key, state.store[key]);
  });
}
if (clearAllBtn) {
  clearAllBtn.addEventListener('click', async () => {
    const key = ymd(state.selected);
    const arr = getTodos(key);
    if (arr.length === 0) {
      Swal.fire({
        title: '알림',
        text: '삭제할 할 일이 없습니다.',
        icon: 'info'
      });
      return;
    }

    const result = await Swal.fire({
      title: '모든 할 일 삭제',
      text: '정말로 오늘의 모든 할 일을 삭제할까요?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: '삭제',
      cancelButtonText: '취소'
    });

    if (!result.isConfirmed) return;
    for (const it of arr) {
      try {
        await deleteTodoOnServer(it.id);
      } catch (e) {
        console.warn('Failed to delete item', it, e);
      }
    }
    state.store[key] = [];
    setTodos(key, []);

    Swal.fire({
      title: '삭제 완료',
      text: '모든 할 일이 삭제되었습니다.',
      icon: 'success',
      timer: 1500,
      showConfirmButton: false
    });
  });
}

// ===== Theme Toggle Bind =====
if (themeToggle) {
  updateThemeToggleLabel(getTheme());
  themeToggle.addEventListener('click', () => {
    const next = getTheme() === 'dark' ? 'light' : 'dark';
    applyTheme(next);
  });
}

// ===== Init =====
(function init() {
  state.selected = new Date();
  state.current = new Date(state.selected.getFullYear(), state.selected.getMonth(), 1);

  applyTheme(getTheme());

  // 초기 로드: 해당 월 데이터 로드 후 렌더
  loadMonthData(state.current.getFullYear(), state.current.getMonth()).then(() => {
    renderCalendar();
    renderTodos();
  });

  if (window.innerWidth > 900 && todoInput) setTimeout(() => todoInput.focus(), 150);
})();
