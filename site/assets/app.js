/* ==========================================================================
   CreeperConfettiPro Wiki — 前端交互 / Front-end behaviour
   双语切换 · 三态主题（圆形扩散）· 侧栏 · 目录 · 搜索 · 动效 · 下载页
   ========================================================================== */
(function () {
  "use strict";

  var NAV = window.CC_NAV || [];
  var INDEX = window.CC_SEARCH || [];
  var I18N = window.CC_I18N || { en: {}, zh: {} };
  var REPO = window.CC_REPO || "Hi-PolarBear/CreeperConfettiPro";

  var LS_LANG = "cc-lang";
  var LS_THEME = "cc-theme-mode";

  var mqDark = window.matchMedia("(prefers-color-scheme: dark)");
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  var lang = detectLang();
  var themeMode = readThemeMode();
  var blocks = {};
  var tocObserver = null;
  var revealObserver = null;
  var release = null;
  var stats = null;

  var page = (location.pathname.split("/").pop() || "index.html").toLowerCase();
  if (!page) page = "index.html";

  /* ---------------- 工具 ---------------- */
  function t() { return I18N[lang] || I18N.en; }
  function navItem(href) {
    for (var i = 0; i < NAV.length; i++) {
      for (var j = 0; j < NAV[i].items.length; j++) {
        if (NAV[i].items[j].href === href) return NAV[i].items[j];
      }
    }
    return null;
  }
  function flatNav() {
    var out = [];
    NAV.forEach(function (g) { g.items.forEach(function (i) { out.push(i); }); });
    return out;
  }
  function el(tag, cls, html) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (html != null) n.innerHTML = html;
    return n;
  }

  /* ---------------- 语言 ---------------- */
  function detectLang() {
    var q = new URLSearchParams(location.search).get("lang");
    if (q === "zh" || q === "en") return q;
    var saved = null;
    try { saved = localStorage.getItem(LS_LANG); } catch (e) {}
    if (saved === "zh" || saved === "en") return saved;
    return "en"; /* 默认英文 / English by default */
  }

  function applyLang(next, animate) {
    if (next !== "zh" && next !== "en") return;
    lang = next;
    try { localStorage.setItem(LS_LANG, next); } catch (e) {}
    document.documentElement.setAttribute("lang", t().htmlLang || next);

    swapBlocks();
    renderSidebar();
    renderChrome();
    renderPager();
    renderFooter();
    renderTitle();
    buildToc();
    buildGui();
    buildSocial();
    renderStats();
    addAnchors();
    addCopyButtons();
    initReveal();
    updateThemeButton();
    updateLangButton();

    if (release) renderRelease(release);
    if (window.__refreshSearch) window.__refreshSearch();
  }

  function swapBlocks() {
    var main = document.querySelector(".main");
    if (!main) return;
    var pager = document.getElementById("pager");
    Object.keys(blocks).forEach(function (key) {
      var node = blocks[key];
      if (key === lang) {
        if (!node.parentNode) main.insertBefore(node, pager || null);
        node.style.animation = "none";
        void node.offsetWidth;
        node.style.animation = "";
      } else if (node.parentNode) {
        node.parentNode.removeChild(node);
      }
    });
  }

  function updateLangButton() {
    var btn = document.getElementById("langBtn");
    if (!btn) return;
    btn.innerHTML =
      '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round">' +
      '<circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c2.5 2.6 2.5 15.4 0 18M12 3c-2.5 2.6-2.5 15.4 0 18"/></svg>' +
      "<span>" + t().langBtn + "</span>";
    btn.title = t().langTitle;
    btn.setAttribute("aria-label", t().langTitle);
  }

  /* ---------------- 主题 ---------------- */
  function readThemeMode() {
    var m = new URLSearchParams(location.search).get("theme");
    if (m !== "light" && m !== "dark" && m !== "auto") {
      try { m = localStorage.getItem(LS_THEME); } catch (e) { m = null; }
    }
    return m === "light" || m === "dark" || m === "auto" ? m : "auto";
  }

  function resolved(mode) {
    return mode === "auto" ? (mqDark.matches ? "dark" : "light") : mode;
  }

  function applyTheme(mode, originEvent) {
    themeMode = mode;
    try { localStorage.setItem(LS_THEME, mode); } catch (e) {}
    var next = resolved(mode);
    var applied = false;

    function set() {
      if (applied) return;
      applied = true;
      document.documentElement.setAttribute("data-theme", next);
      updateThemeButton();
    }

    if (!document.startViewTransition || reduceMotion) { set(); return; }

    var x = originEvent && originEvent.clientX != null ? originEvent.clientX : window.innerWidth - 60;
    var y = originEvent && originEvent.clientY != null ? originEvent.clientY : 40;
    var r = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y));

    try {
      var vt = document.startViewTransition(set);
      if (vt && vt.ready) {
        vt.ready.then(function () {
          try {
            document.documentElement.animate(
              { clipPath: ["circle(0px at " + x + "px " + y + "px)", "circle(" + r + "px at " + x + "px " + y + "px)"] },
              { duration: 560, easing: "cubic-bezier(.4,0,.2,1)", pseudoElement: "::view-transition-new(root)" }
            );
          } catch (e) {}
        }).catch(set);
      }
      /* 保险：某些环境（无渲染帧的嵌入式内核）不会执行更新回调，超时后直接落地 */
      window.setTimeout(set, 260);
    } catch (e) {
      set();
    }
  }

  var THEME_ICON = {
    auto: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><circle cx="12" cy="12" r="9"/><path d="M12 3a9 9 0 0 0 0 18z" fill="currentColor" stroke="none"/></svg>',
    light: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
    dark: '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 12.8A8.5 8.5 0 1111.2 3a6.6 6.6 0 009.8 9.8z"/></svg>'
  };

  function updateThemeButton() {
    var btn = document.getElementById("themeBtn");
    if (!btn) return;
    btn.innerHTML = THEME_ICON[themeMode];
    var label = themeMode === "auto" ? t().themeAuto : themeMode === "dark" ? t().themeDark : t().themeLight;
    btn.title = label;
    btn.setAttribute("aria-label", label);
  }

  function cycleTheme(e) {
    var order = ["auto", "light", "dark"];
    var next = order[(order.indexOf(themeMode) + 1) % order.length];
    /* 自动模式下第一次点击直接切到「当前的反面」，避免点了没反应 */
    if (themeMode === "auto") next = resolved("auto") === "dark" ? "light" : "dark";
    if (resolved(next) === resolved(themeMode) && next !== "auto") next = "auto";
    applyTheme(next, e);
  }

  /* ---------------- 顶栏 ---------------- */
  function buildTopbar() {
    var bar = document.getElementById("topbar");
    if (!bar) return;
    bar.className = "topbar";
    bar.innerHTML =
      '<button class="iconbtn menu-btn" id="menuBtn" aria-label="Menu">' +
        '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><path d="M4 7h16M4 12h16M4 17h16"/></svg>' +
      "</button>" +
      '<a class="brand" href="index.html">' +
        '<img class="brand-mark" id="brandLogo" alt="CreeperConfettiPro" src="https://cdn.modrinth.com/data/zOtyOPVB/bef4cc1825b9c005b7df620b4ca0ba039c67a660.jpeg">' +
        "<span>CreeperConfettiPro</span>" +
        '<span class="brand-sub" id="brandSub"></span>' +
      "</a>" +
      '<div class="topbar-right">' +
        '<div class="search">' +
          '<span class="search-icon"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.6-3.6"/></svg></span>' +
          '<input id="searchInput" type="text" autocomplete="off" spellcheck="false" aria-label="Search">' +
          "<kbd>/</kbd>" +
          '<div class="search-results" id="searchResults"></div>' +
        "</div>" +
        '<button class="iconbtn langbtn" id="langBtn"></button>' +
        '<button class="iconbtn" id="themeBtn"></button>' +
        '<a class="iconbtn ghbtn" id="ghBtn" href="https://github.com/Hi-PolarBear/CreeperConfettiPro"' +
          ' target="_blank" rel="noopener" aria-label="GitHub" title="GitHub">' +
          '<svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">' +
            '<path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577' +
            ' 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7' +
            'c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998' +
            '.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22' +
            '-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405' +
            ' 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22' +
            ' 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286' +
            ' 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/></svg>' +
        "</a>" +
      "</div>";

    var logo = document.getElementById("brandLogo");
    if (logo) {
      logo.addEventListener("error", function () {
        var span = document.createElement("span");
        span.className = "brand-mark";
        span.style.display = "grid";
        span.style.placeItems = "center";
        span.style.background = "linear-gradient(150deg,#34c47c,#0e7a41)";
        span.style.color = "#fff";
        span.style.fontSize = "12px";
        span.style.fontWeight = "800";
        span.textContent = "CC";
        logo.replaceWith(span);
      });
    }

    document.getElementById("themeBtn").addEventListener("click", cycleTheme);
    document.getElementById("langBtn").addEventListener("click", function (e) {
      var next = lang === "en" ? "zh" : "en";
      applyLang(next);
      var btn = e.currentTarget;
      if (btn && btn.animate) {
        btn.animate([{ transform: "scale(1)" }, { transform: "scale(.9)" }, { transform: "scale(1)" }], { duration: 260 });
      }
    });

    document.getElementById("menuBtn").addEventListener("click", function () {
      var sb = document.getElementById("sidebar");
      var ov = document.getElementById("overlay");
      var open = sb.classList.toggle("open");
      if (ov) ov.classList.toggle("show", open);
    });

    window.addEventListener("scroll", function () {
      bar.classList.toggle("scrolled", window.scrollY > 8);
    }, { passive: true });
  }

  /* ---------------- 侧边栏 ---------------- */
  function renderSidebar() {
    var sb = document.getElementById("sidebar");
    if (!sb) return;
    var html = '<nav class="nav">';
    NAV.forEach(function (group) {
      html += '<div class="nav-group"><div class="nav-group-title">' + group.title[lang] + "</div>";
      group.items.forEach(function (item) {
        var cls = item.href.toLowerCase() === page ? ' class="active"' : "";
        html += '<a href="' + item.href + '"' + cls + ">" + item.label[lang] + "</a>";
      });
      html += "</div>";
    });
    html += "</nav>";
    sb.innerHTML = html;
    sb.querySelectorAll("a").forEach(function (a) {
      a.addEventListener("click", function () {
        sb.classList.remove("open");
        var ov = document.getElementById("overlay");
        if (ov) ov.classList.remove("show");
      });
    });
  }

  /* ---------------- 顶栏文案 / 标题 / 页脚 ---------------- */
  function renderChrome() {
    var sub = document.getElementById("brandSub");
    if (sub) sub.textContent = t().brandSub;
    var input = document.getElementById("searchInput");
    if (input) input.placeholder = t().searchPlaceholder;
    var mb = document.getElementById("menuBtn");
    if (mb) mb.setAttribute("aria-label", t().menu);
  }

  function renderTitle() {
    var item = navItem(page);
    if (item && item.title) document.title = item.title[lang];
  }

  function renderPager() {
    var pager = document.getElementById("pager");
    if (!pager) return;
    var list = flatNav();
    var i = -1;
    for (var k = 0; k < list.length; k++) if (list[k].href === page) { i = k; break; }
    if (i < 0) { pager.innerHTML = ""; return; }
    var prev = list[i - 1], next = list[i + 1];
    var html = "";
    if (prev) {
      html += '<a href="' + prev.href + '"><div class="p-label">' + t().prev + '</div><div class="p-title">← ' + prev.label[lang] + "</div></a>";
    }
    if (next) {
      html += '<a class="next" href="' + next.href + '"><div class="p-label">' + t().next + '</div><div class="p-title">' + next.label[lang] + " →</div></a>";
    }
    pager.innerHTML = html;
  }

  function renderFooter() {
    var foot = document.getElementById("foot");
    if (!foot) return;
    foot.innerHTML = t().footer.map(function (s) { return "<span>" + s + "</span>"; }).join("") +
      '<div class="foot-social" data-social="footer"></div>';
  }

  /* ---------------- 社交 / 平台入口 ---------------- */
  var SOCIAL = [
    { k: "discord", href: "https://discord.gg/DajmZMmEbx" },
    { k: "github", href: "https://github.com/Hi-PolarBear/CreeperConfettiPro" },
    { k: "modrinth", href: "https://modrinth.com/plugin/creeperconfettipro" },
    { k: "hangar", href: "https://hangar.papermc.io/Hi-PolarBear/CreeperConfettiPro" },
    { k: "spigot", href: "https://www.spigotmc.org/resources/creeperconfettipro.133573" }
  ];

  function buildSocial() {
    var labels = t().social || {};
    document.querySelectorAll("[data-social]").forEach(function (box) {
      var compact = box.getAttribute("data-social") === "footer";
      box.innerHTML = SOCIAL.map(function (item) {
        return '<a class="link-pill ' + item.k + (compact ? " sm" : "") + '" href="' + item.href +
          '" target="_blank" rel="noopener">' +
          '<span class="lp-dot"></span>' + (labels[item.k] || item.k) +
          '<span class="lp-arrow">↗</span></a>';
      }).join("");
    });
  }

  /* ---------------- bStats 实时数据 ---------------- */
  var BSTATS_URL = "https://bstats.org/plugin/bukkit/CreeperConfettiPro/29666";
  var BSTATS_API = "https://bstats.org/api/v1/plugins/29666/charts/";

  function fmtNum(n) {
    if (n == null || isNaN(n)) return "—";
    if (n >= 10000) return (n / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    return String(n);
  }

  function initStats() {
    var box = document.querySelector("[data-bstats]");
    if (!box) return;
    var keys = ["servers", "players"];
    var results = {};
    var pending = keys.length;

    keys.forEach(function (key) {
      fetch(BSTATS_API + key + "/data?maxElements=2", { headers: { Accept: "application/json" } })
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (arr) {
          if (Object.prototype.toString.call(arr) === "[object Array]" && arr.length) {
            var last = arr[arr.length - 1];
            var prev = arr.length > 1 ? arr[arr.length - 2] : null;
            results[key] = { value: last[1], delta: prev ? last[1] - prev[1] : 0 };
          }
        })
        .catch(function () {})
        .then(function () {
          if (--pending === 0) {
            stats = results;
            renderStats();
          }
        });
    });
  }

  function renderStats() {
    var box = document.querySelector("[data-bstats]");
    if (!box) return;
    var S = t().stats || {};

    function card(label, data) {
      if (!data) return "";
      var delta = data.delta || 0;
      var chip = delta === 0 ? "" :
        '<span class="stat-delta ' + (delta > 0 ? "up" : "down") + '">' +
        (delta > 0 ? "▲ " : "▼ ") + fmtNum(Math.abs(delta)) + "</span>";
      return '<div class="stat-card">' +
        '<div class="stat-num">' + fmtNum(data.value) + chip + "</div>" +
        '<div class="stat-label">' + label + "</div></div>";
    }

    var html = "";
    if (stats) {
      html += card(S.servers, stats.servers);
      html += card(S.players, stats.players);
    }
    html += '<a class="stat-card stat-link" href="' + BSTATS_URL + '" target="_blank" rel="noopener">' +
      '<div class="stat-num small">bStats' + (stats ? "" : " · " + (S.failed || "")) + "</div>" +
      '<div class="stat-label">' + (S.source || "bStats") + " ↗</div></a>";

    box.innerHTML = html;
    box.style.display = "";
  }

  /* ---------------- 图形界面示意图 ---------------- */
  var GUI_LIST = {
    45: { cls: "act", key: "prev" },
    46: { cls: "num", key: "page" },
    48: { cls: "act", key: "even" },
    49: { cls: "act", key: "add" },
    53: { cls: "act", key: "next" }
  };
  var GUI_EDIT = {
    4: { cls: "act", key: "preview" },
    10: { cls: "act", text: "−10%" },
    11: { cls: "act", text: "−5%" },
    12: { cls: "act", text: "−1%" },
    13: { cls: "num", key: "chance" },
    14: { cls: "act", text: "+1%" },
    15: { cls: "act", text: "+5%" },
    16: { cls: "act", text: "+10%" },
    18: { cls: "act", key: "setHand" },
    22: { cls: "act", key: "delete" },
    26: { cls: "act", key: "back" }
  };

  function buildGui() {
    var g = t().gui || {};
    document.querySelectorAll("[data-gui]").forEach(function (grid) {
      var kind = grid.getAttribute("data-gui");
      var edit = kind === "edit";
      var total = edit ? 27 : 54;
      var map = edit ? GUI_EDIT : GUI_LIST;
      var html = "";
      for (var i = 0; i < total; i++) {
        var conf = map[i];
        if (!conf && !edit && i < 45) conf = { cls: "style" };
        if (!conf) { html += '<div class="gslot filler"></div>'; continue; }
        if (conf.cls === "style") {
          html += '<div class="gslot style" title="' + (g.slot || "Style") + " " + (i + 1) + '"></div>';
          continue;
        }
        var label = conf.text || g[conf.key] || "";
        html += '<div class="gslot ' + conf.cls + '" title="' + label + '">' + label + "</div>";
      }
      grid.innerHTML = html;
    });
  }

  /* ---------------- 目录 ---------------- */
  function buildToc() {
    var toc = document.getElementById("toc");
    var block = blocks[lang];
    if (!toc || !block) return;

    var heads = block.querySelectorAll("h2[id], h3[id]");
    if (!heads.length) { toc.innerHTML = ""; return; }

    var html = '<div class="toc-title">' + t().toc + "</div><ul>";
    Array.prototype.forEach.call(heads, function (h) {
      var cls = h.tagName === "H3" ? ' class="lv3"' : "";
      html += '<li><a href="#' + h.id + '"' + cls + ">" + h.textContent.replace("#", "").trim() + "</a></li>";
    });
    toc.innerHTML = html + "</ul>";

    var links = toc.querySelectorAll("a");
    var map = {};
    Array.prototype.forEach.call(links, function (a) { map[a.getAttribute("href").slice(1)] = a; });

    if (tocObserver) tocObserver.disconnect();
    if (!("IntersectionObserver" in window)) return;
    tocObserver = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (!en.isIntersecting) return;
        Array.prototype.forEach.call(links, function (a) { a.classList.remove("active"); });
        var a = map[en.target.id];
        if (a) a.classList.add("active");
      });
    }, { rootMargin: "-72px 0px -70% 0px", threshold: 0 });
    Array.prototype.forEach.call(heads, function (h) { tocObserver.observe(h); });
  }

  /* ---------------- 标题锚点 ---------------- */
  function addAnchors() {
    var block = blocks[lang];
    if (!block) return;
    block.querySelectorAll("h2[id], h3[id]").forEach(function (h) {
      if (h.querySelector(".anchor-link")) return;
      var a = document.createElement("a");
      a.className = "anchor-link";
      a.href = "#" + h.id;
      a.textContent = "#";
      a.setAttribute("aria-hidden", "true");
      h.appendChild(a);
    });
  }

  /* ---------------- 滚动进场 ---------------- */
  function initReveal() {
    var block = blocks[lang];
    if (!block) return;

    var targets = block.querySelectorAll(".reveal, h2[id]");
    if (revealObserver) revealObserver.disconnect();

    if (reduceMotion || !("IntersectionObserver" in window)) {
      targets.forEach(function (n) { n.classList.add("in"); });
      return;
    }

    revealObserver = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) {
          en.target.classList.add("in");
          revealObserver.unobserve(en.target);
        }
      });
    }, { rootMargin: "0px 0px -8% 0px", threshold: 0.08 });

    targets.forEach(function (n, i) {
      if (n.classList.contains("reveal") && !n.style.getPropertyValue("--d")) {
        n.style.setProperty("--d", Math.min(i * 40, 240) + "ms");
      }
      revealObserver.observe(n);
    });
  }

  /* ---------------- 搜索 ---------------- */
  function buildSearch() {
    var input = document.getElementById("searchInput");
    var box = document.getElementById("searchResults");
    if (!input || !box) return;

    var active = -1;

    function entries() {
      return INDEX.map(function (row) {
        var item = navItem(row[0]);
        var isEn = lang === "en";
        return {
          href: row[0] + row[1],
          section: isEn ? row[2] : row[4],
          pageTitle: item ? item.label[lang] : row[0],
          hay: (row[2] + " " + row[3] + " " + row[4] + " " + row[5] + " " + (item ? item.label.en + " " + item.label.zh : "")).toLowerCase()
        };
      });
    }

    var DATA = entries();

    function render(list) {
      if (!list.length) {
        box.innerHTML = '<div class="r-empty">' + t().searchEmpty + "</div>";
        box.classList.add("open");
        return;
      }
      box.innerHTML = list.map(function (item) {
        return '<a href="' + item.href + '"><div class="r-title">' + item.section +
          '</div><div class="r-meta">' + item.pageTitle + "</div></a>";
      }).join("");
      box.classList.add("open");
      active = -1;
    }

    function query(q) {
      q = q.trim().toLowerCase();
      if (!q) { box.classList.remove("open"); box.innerHTML = ""; return; }
      var terms = q.split(/\s+/);
      var scored = DATA.map(function (item) {
        var score = 0;
        for (var i = 0; i < terms.length; i++) {
          if (item.hay.indexOf(terms[i]) === -1) return null;
          if (item.section.toLowerCase().indexOf(terms[i]) !== -1) score += 3;
          score += 1;
        }
        return { item: item, score: score };
      }).filter(Boolean);
      scored.sort(function (a, b) { return b.score - a.score; });
      render(scored.slice(0, 9).map(function (s) { return s.item; }));
    }

    input.addEventListener("input", function () { query(input.value); });
    input.addEventListener("focus", function () { if (input.value.trim()) query(input.value); });

    input.addEventListener("keydown", function (e) {
      var links = box.querySelectorAll("a");
      if (e.key === "ArrowDown" || e.key === "ArrowUp") {
        e.preventDefault();
        if (!links.length) return;
        active = e.key === "ArrowDown" ? (active + 1) % links.length : (active - 1 + links.length) % links.length;
        links.forEach(function (a, i) { a.classList.toggle("active", i === active); });
        links[active].scrollIntoView({ block: "nearest" });
      } else if (e.key === "Enter") {
        if (links.length) { e.preventDefault(); (links[active >= 0 ? active : 0]).click(); }
      } else if (e.key === "Escape") {
        box.classList.remove("open");
        input.blur();
      }
    });

    document.addEventListener("click", function (e) {
      if (!e.target.closest(".search")) box.classList.remove("open");
    });

    document.addEventListener("keydown", function (e) {
      var tag = document.activeElement.tagName;
      if (e.key === "/" && !/^(INPUT|TEXTAREA)$/.test(tag)) {
        e.preventDefault();
        input.focus();
        input.select();
      }
    });

    window.__refreshSearch = function () { DATA = entries(); };
  }

  /* ---------------- 代码复制 ---------------- */
  function addCopyButtons() {
    var block = blocks[lang];
    if (!block) return;
    block.querySelectorAll("pre").forEach(function (pre) {
      if (pre.querySelector(".copy-btn")) return;
      var code = pre.querySelector("code");
      if (!code) return;
      var btn = document.createElement("button");
      btn.className = "copy-btn";
      btn.type = "button";
      btn.textContent = t().copy;
      btn.addEventListener("click", function () {
        var text = code.textContent;
        var done = function () {
          btn.textContent = t().copied;
          btn.classList.add("done");
          setTimeout(function () { btn.textContent = t().copy; btn.classList.remove("done"); }, 1600);
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(text).then(done, function () { fallback(text, done); });
        } else {
          fallback(text, done);
        }
      });
      pre.appendChild(btn);
    });
  }

  function fallback(text, done) {
    var ta = document.createElement("textarea");
    ta.value = text;
    ta.style.position = "fixed";
    ta.style.opacity = "0";
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand("copy"); done(); } catch (e) {}
    document.body.removeChild(ta);
  }

  /* ---------------- 卡片光斑 ---------------- */
  function initCardGlow() {
    document.addEventListener("mousemove", function (e) {
      var card = e.target.closest && e.target.closest(".card, .dl-card");
      if (!card) return;
      var r = card.getBoundingClientRect();
      card.style.setProperty("--mx", (e.clientX - r.left) + "px");
      card.style.setProperty("--my", (e.clientY - r.top) + "px");
    }, { passive: true });
  }

  /* ---------------- 遮罩 ---------------- */
  function addOverlay() {
    if (document.getElementById("overlay")) return;
    var ov = document.createElement("div");
    ov.id = "overlay";
    ov.className = "overlay";
    ov.addEventListener("click", function () {
      document.getElementById("sidebar").classList.remove("open");
      ov.classList.remove("show");
    });
    document.body.appendChild(ov);
  }

  /* ---------------- 下载页 ---------------- */
  function fmtSize(bytes) {
    if (!bytes && bytes !== 0) return "—";
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
    return (bytes / 1024 / 1024).toFixed(2) + " MB";
  }

  function fmtDate(iso) {
    if (!iso) return "—";
    try {
      return new Date(iso).toLocaleDateString(lang === "zh" ? "zh-CN" : "en-US", { year: "numeric", month: "short", day: "numeric" });
    } catch (e) { return iso.slice(0, 10); }
  }

  /* ---------------- Markdown → HTML（仅用于发行说明，覆盖 GitHub 常见语法） ---------------- */
  function mdToHtml(src) {
    function esc(v) {
      return String(v).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
    function inline(v) {
      var s = esc(v);
      s = s.replace(/`([^`]+)`/g, function (_, c) { return "<code>" + c + "</code>"; });
      s = s.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
      s = s.replace(/(^|[\s(])\*([^*\n]+)\*/g, "$1<em>$2</em>");
      s = s.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, function (_, txt, url) {
        return '<a href="' + url + '" target="_blank" rel="noopener">' + txt + "</a>";
      });
      return s;
    }
    function isTableSep(line) {
      return line.indexOf("|") !== -1 && /^\s*\|?[\s:|-]*-[\s:|-]*\|?\s*$/.test(line);
    }
    function cells(line) {
      var s = line.trim();
      if (s.charAt(0) === "|") s = s.slice(1);
      if (s.charAt(s.length - 1) === "|") s = s.slice(0, -1);
      return s.split("|").map(function (c) { return c.trim(); });
    }

    var lines = String(src || "").replace(/\r\n?/g, "\n").split("\n");
    var out = [];
    var i = 0;

    while (i < lines.length) {
      var line = lines[i];

      if (!line.trim()) { i++; continue; }

      if (/^\s*```/.test(line)) {
        var buf = [];
        i++;
        while (i < lines.length && !/^\s*```/.test(lines[i])) { buf.push(lines[i]); i++; }
        i++;
        out.push("<pre><code>" + esc(buf.join("\n")) + "</code></pre>");
        continue;
      }

      var h = line.match(/^(#{1,6})\s+(.*)$/);
      if (h) {
        var lvl = Math.min(2 + h[1].length, 6);
        out.push("<h" + lvl + ">" + inline(h[2]) + "</h" + lvl + ">");
        i++;
        continue;
      }

      if (/^\s*([-*_])\s*\1\s*\1[\s\-*_]*$/.test(line)) { out.push("<hr>"); i++; continue; }

      if (/^\s*>\s?/.test(line)) {
        var q = [];
        while (i < lines.length && /^\s*>\s?/.test(lines[i])) {
          q.push(lines[i].replace(/^\s*>\s?/, ""));
          i++;
        }
        out.push("<blockquote>" + inline(q.join(" ")) + "</blockquote>");
        continue;
      }

      if (line.indexOf("|") !== -1 && i + 1 < lines.length && isTableSep(lines[i + 1])) {
        var head = cells(line);
        i += 2;
        var rows = [];
        while (i < lines.length && lines[i].trim() && lines[i].indexOf("|") !== -1) {
          rows.push(cells(lines[i]));
          i++;
        }
        var t = "<table><thead><tr>" + head.map(function (c) { return "<th>" + inline(c) + "</th>"; }).join("") + "</tr></thead><tbody>";
        rows.forEach(function (r) {
          t += "<tr>" + r.map(function (c) { return "<td>" + inline(c) + "</td>"; }).join("") + "</tr>";
        });
        out.push(t + "</tbody></table>");
        continue;
      }

      if (/^\s*[-*+]\s+/.test(line)) {
        var ul = [];
        while (i < lines.length && /^\s*[-*+]\s+/.test(lines[i])) {
          ul.push(lines[i].replace(/^\s*[-*+]\s+/, ""));
          i++;
        }
        out.push("<ul>" + ul.map(function (x) { return "<li>" + inline(x) + "</li>"; }).join("") + "</ul>");
        continue;
      }

      if (/^\s*\d+[.)]\s+/.test(line)) {
        var ol = [];
        while (i < lines.length && /^\s*\d+[.)]\s+/.test(lines[i])) {
          ol.push(lines[i].replace(/^\s*\d+[.)]\s+/, ""));
          i++;
        }
        out.push("<ol>" + ol.map(function (x) { return "<li>" + inline(x) + "</li>"; }).join("") + "</ol>");
        continue;
      }

      var p = [];
      while (i < lines.length && lines[i].trim() &&
             !/^\s*(#{1,6}\s|```|>\s?|[-*+]\s|\d+[.)]\s)/.test(lines[i]) &&
             !(lines[i].indexOf("|") !== -1 && i + 1 < lines.length && isTableSep(lines[i + 1]))) {
        p.push(lines[i]);
        i++;
      }
      if (p.length) out.push("<p>" + inline(p.join(" ")) + "</p>");
      else i++;
    }
    return out.join("\n");
  }

  function initDownload(soft) {
    var grid = document.getElementById("dlGrid");
    if (!grid) return;
    var T = t().dl;

    if (!soft || !release) {
      grid.innerHTML = '<div class="dl-skeleton"></div><div class="dl-skeleton"></div>';
      var meta = document.getElementById("dlMeta");
      if (meta) meta.innerHTML = '<span class="dl-note">' + T.loading + "</span>";
      var err = document.getElementById("dlError");
      if (err) err.remove();
      var notes = document.getElementById("dlNotes");
      if (notes) notes.textContent = "";
    }

    fetch("https://api.github.com/repos/" + REPO + "/releases/latest", {
      headers: { Accept: "application/vnd.github+json" }
    }).then(function (res) {
      if (!res.ok) throw new Error("HTTP " + res.status);
      return res.json();
    }).then(function (data) {
      release = data;
      renderRelease(data);
    }).catch(function (e) {
      renderDownloadError(e);
    });
  }

  function renderRelease(rel) {
    var grid = document.getElementById("dlGrid");
    if (!grid) return;
    var T = t().dl;
    var sep = lang === "zh" ? "：" : ": ";
    var assets = rel.assets || [];

    var bukkit = assets.filter(function (a) { return /bukkit/i.test(a.name); })[0];
    var folia = assets.filter(function (a) { return /folia/i.test(a.name); })[0];
    var others = assets.filter(function (a) { return a !== bukkit && a !== folia; });

    var meta = document.getElementById("dlMeta");
    if (meta) {
      meta.innerHTML =
        '<span class="pill"><span class="badge-dot"></span>' + T.latestPill + sep + (rel.tag_name || rel.name) + "</span>" +
        "<span>" + T.published + sep + fmtDate(rel.published_at) + "</span>" +
        (rel.html_url ? '<a href="' + rel.html_url + '" target="_blank" rel="noopener">' + T.viewRelease + " ↗</a>" : "");
    }

    function card(title, sub, asset, primary) {
      if (!asset) {
        return '<div class="dl-card"><h3>' + title + '</h3><p class="dl-sub">' + sub + '</p>' +
          '<p class="dl-note">' + T.noAssets + "</p></div>";
      }
      return '<div class="dl-card">' +
        "<h3>" + title + "</h3>" +
        '<p class="dl-sub">' + sub + "</p>" +
        '<div class="dl-file">' + asset.name + "</div>" +
        '<div class="dl-stats"><span>' + T.size + sep + "<b>" + fmtSize(asset.size) + "</b></span></div>" +
        '<a class="btn ' + (primary ? "btn-primary" : "btn-ghost") + '" href="' + asset.browser_download_url +
        '" target="_blank" rel="noopener">' +
        '<svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12M7 11l5 5 5-5M4 20h16"/></svg>' +
        T.download + "</a>" +
        "</div>";
    }

    var zh = lang === "zh";
    var html = "";
    html += card("Bukkit", zh ? "Spigot / Paper / Purpur · Java 8+" : "Spigot / Paper / Purpur · Java 8+", bukkit, true);
    html += card("Folia", zh ? "Folia 1.19.4+ · Java 17+" : "Folia 1.19.4+ · Java 17+", folia, true);

    if (others.length) {
      others.forEach(function (a) {
        html += '<div class="dl-card"><h3>' + a.name + '</h3><p class="dl-sub">' + T.otherAssets + "</p>" +
          '<div class="dl-stats"><span>' + T.size + sep + "<b>" + fmtSize(a.size) + "</b></span></div>" +
          '<a class="btn btn-ghost" href="' + a.browser_download_url + '" target="_blank" rel="noopener">' + T.download + "</a></div>";
      });
    }

    grid.innerHTML = html;

    var err = document.getElementById("dlError");
    if (err) err.remove();

    var notes = document.getElementById("dlNotes");
    if (notes) {
      notes.innerHTML = rel.body ? mdToHtml(rel.body) : "";
      notes.style.display = rel.body ? "" : "none";
    }
  }

  function renderDownloadError(e) {
    var grid = document.getElementById("dlGrid");
    var T = t().dl;
    if (grid) grid.innerHTML = "";
    var meta = document.getElementById("dlMeta");
    if (meta) meta.innerHTML = "";
    var main = document.querySelector(".main");
    var box = document.getElementById("dlError");
    if (!box && main) {
      box = el("div", "dl-error");
      box.id = "dlError";
      var anchor = document.getElementById("dlGrid");
      anchor.parentNode.insertBefore(box, anchor);
    }
    if (box) {
      box.innerHTML = "<p><strong>" + T.errorTitle + "</strong></p><p>" + T.errorBody +
        (e && e.message ? " <code>" + e.message + "</code>" : "") + "</p>" +
        '<button class="btn btn-ghost" id="dlRetry" type="button">' + T.retry + "</button> " +
        '<a class="btn btn-ghost" href="https://github.com/' + REPO + '/releases/latest" target="_blank" rel="noopener">' + T.viewRelease + " ↗</a>";
      var retry = document.getElementById("dlRetry");
      if (retry) retry.addEventListener("click", function () { initDownload(); });
    }
    var notes = document.getElementById("dlNotes");
    if (notes) notes.textContent = "";
  }

  /* ---------------- 启动 ---------------- */
  function init() {
    document.querySelectorAll(".lang-block[data-lang]").forEach(function (node) {
      blocks[node.getAttribute("data-lang")] = node;
    });

    /* 离线时隐藏外链 logo，避免出现破图 */
    document.querySelectorAll(".hero-logo").forEach(function (img) {
      img.addEventListener("error", function () { img.style.display = "none"; });
    });

    buildTopbar();
    addOverlay();
    initCardGlow();
    buildSearch();
    applyLang(lang, false);

    if (mqDark.addEventListener) {
      mqDark.addEventListener("change", function () {
        if (themeMode === "auto") {
          document.documentElement.setAttribute("data-theme", resolved("auto"));
        }
      });
    }

    if (document.getElementById("dlGrid")) initDownload();
    initStats();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
