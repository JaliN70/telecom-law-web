(function () {
  'use strict';

  const app = document.getElementById('app');
  const backBtn = document.getElementById('backBtn');
  const pageTitle = document.getElementById('pageTitle');
  const pageSub = document.getElementById('pageSub');
  const headerMojLink = document.getElementById('headerMojLink');
  const toast = document.getElementById('toast');

  document.getElementById('sysDate').textContent = LAW.updated;
  document.getElementById('sysAuthor').textContent = LAW.author;
  document.getElementById('lawDate').textContent = LAW.amended;

  let toastTimer;

  function showToast(msg) {
    toast.textContent = msg;
    toast.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('show'), 2200);
  }

  function formatArticleNum(art) {
    return art;
  }

  function articleDisplayTitle(art) {
    return `第 ${art} 條`;
  }

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function isPackagedApp() {
    return location.protocol === 'file:';
  }

  function externalLinkAttrs() {
    return isPackagedApp() ? '' : ' target="_blank" rel="noopener"';
  }

  function openExternal(url) {
    if (window.Android && typeof Android.openExternal === 'function') {
      Android.openExternal(url);
      return;
    }
    window.open(url, '_blank') || (window.location.href = url);
  }

  function bindExternalLinks(root) {
    if (!root) return;
    root.querySelectorAll('a[href^="http"]').forEach((link) => {
      link.removeAttribute('target');
      link.addEventListener('click', (e) => {
        if (!isPackagedApp()) return;
        e.preventDefault();
        openExternal(link.href);
      });
    });
  }

  function categoryCount(cat) {
    if (cat.files) return `${ATTACHMENTS.length} 份截圖`;
    return `${cat.articles.length} 項法條`;
  }

  function attachmentViewUrl(id) {
    return `attachment-view.html?id=${encodeURIComponent(id)}`;
  }

  function renderHome() {
    backBtn.hidden = true;
    pageTitle.textContent = LAW.shortName;
    pageSub.textContent = LAW.name;
    headerMojLink.href = LAW.fullUrl;

    const totalArts = CATEGORIES.reduce((n, c) => n + (c.articles ? c.articles.length : 0), 0);

    const cards = CATEGORIES.map((cat, i) => `
      <li class="fade-up" style="animation-delay:${i * 0.03}s">
        <button type="button" class="cat-card" data-cat="${cat.id}" aria-label="${escapeHtml(cat.title)}">
          <span class="cat-icon" aria-hidden="true">${cat.icon}</span>
          <h2 class="cat-title">${escapeHtml(cat.title)}</h2>
          <p class="cat-sub">${escapeHtml(cat.subtitle)}</p>
          <span class="cat-count">${categoryCount(cat)}</span>
        </button>
      </li>
    `).join('');

    app.innerHTML = `
      <section class="hero fade-up">
        <span class="hero-eyebrow">Building Telecom Code</span>
        <h2 class="hero-title">${escapeHtml(LAW.name)}</h2>
        <p class="hero-desc">共 ${CATEGORIES.length - 1} 類 · ${totalArts} 條，依主題分類連結全國法規資料庫</p>
        <a class="hero-cta" href="${LAW.fullUrl}"${externalLinkAttrs()}>
          完整法規
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
        </a>
      </section>
      <p class="section-label">主題分類</p>
      <ul class="cat-grid">${cards}</ul>
    `;

    app.querySelectorAll('.cat-card').forEach((btn) => {
      btn.addEventListener('click', () => navigate(`#/${btn.dataset.cat}`));
    });
    bindExternalLinks(app);
  }

  function renderAttachments(cat) {
    const itemsHtml = ATTACHMENTS.map(
      (item, i) => `
      <li class="fade-up" style="animation-delay:${i * 0.025}s">
        <a class="art-btn" href="${attachmentViewUrl(item.id)}"
           data-label="${escapeHtml(item.label)}">
          <span class="art-num">圖</span>
          <span class="art-label">${escapeHtml(item.label)}</span>
          <svg class="art-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <path d="M9 18l6-6-6-6"/>
          </svg>
        </a>
      </li>
    `
    ).join('');

    app.innerHTML = `
      <header class="cat-header fade-up">
        <div class="cat-header-icon" aria-hidden="true">${cat.icon}</div>
        <h2 class="cat-header-title">${escapeHtml(cat.title)}</h2>
        <p class="cat-header-sub">${escapeHtml(cat.subtitle)}</p>
        <p class="cat-header-desc">${escapeHtml(cat.desc)}</p>
      </header>
      <p class="section-label">離線查閱 · 附圖向量、附件高清</p>
      <ul class="art-list">${itemsHtml}</ul>
    `;

    app.querySelectorAll('.art-btn').forEach((link) => {
      link.addEventListener('click', () => showToast(`查看 ${link.dataset.label}`));
    });
  }

  function renderCategory(catId) {
    const cat = CATEGORIES.find((c) => c.id === catId);
    if (!cat) {
      navigate('#/');
      return;
    }

    backBtn.hidden = false;
    pageTitle.textContent = cat.title;
    pageSub.textContent = cat.subtitle;
    headerMojLink.href = LAW.fullUrl;

    if (cat.files) {
      renderAttachments(cat);
      return;
    }

    const articlesHtml = cat.articles
      .map(
        (item, i) => `
        <li class="fade-up" style="animation-delay:${i * 0.025}s">
          <a class="art-btn" href="${mojArticleUrl(item.art)}"${externalLinkAttrs()}
             data-art="${escapeHtml(item.art)}">
            <span class="art-num">${escapeHtml(formatArticleNum(item.art))}</span>
            <span class="art-label">
              ${escapeHtml(item.label)}
              ${item.note ? `<span class="art-note art-note--link">${escapeHtml(item.note)}</span>` : ''}
            </span>
            <svg class="art-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/>
              <polyline points="15 3 21 3 21 9"/>
              <line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
          </a>
        </li>
      `
      )
      .join('');

    app.innerHTML = `
      <header class="cat-header fade-up">
        <div class="cat-header-icon" aria-hidden="true">${cat.icon}</div>
        <h2 class="cat-header-title">${escapeHtml(cat.title)}</h2>
        <p class="cat-header-sub">${escapeHtml(cat.subtitle)}</p>
        <p class="cat-header-desc">${escapeHtml(cat.desc)}</p>
      </header>
      <p class="section-label">法條連結 · 全國法規資料庫</p>
      <ul class="art-list">${articlesHtml}</ul>
    `;

    app.querySelectorAll('.art-btn').forEach((link) => {
      link.addEventListener('click', () => {
        showToast(`開啟 ${articleDisplayTitle(link.dataset.art)}`);
      });
    });
    bindExternalLinks(app);
  }

  function navigate(hash) {
    if (location.hash !== hash) {
      location.hash = hash;
    } else {
      route();
    }
  }

  function route() {
    const hash = location.hash.replace(/^#\/?/, '') || '';
    const parts = hash.split('/').filter(Boolean);

    if (parts.length === 0) {
      renderHome();
      return;
    }

    renderCategory(parts[0]);
  }

  backBtn.addEventListener('click', () => navigate('#/'));
  window.addEventListener('hashchange', route);
  bindExternalLinks(document);

  route();
})();
