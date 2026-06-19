<template>
  <div class="app">
    <header class="header">
      <div class="container header-content">
        <router-link to="/" class="logo">
          <span class="logo-icon">📻</span>
          <span class="logo-text">怀旧老物件</span>
        </router-link>
        <nav class="nav">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/archives" class="nav-link">年代档案</router-link>
          <router-link to="/favorites" class="nav-link">我的收藏</router-link>
          <router-link to="/publish" class="nav-link publish-btn">发布回忆</router-link>
        </nav>
      </div>
    </header>
    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <footer class="footer">
      <div class="container">
        <p>© 2024 怀旧老物件 - 珍藏每一段时光记忆</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #8b6914 0%, #6b4f0f 100%);
  color: #fff;
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-link {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  transition: color 0.3s;
  position: relative;
}

.nav-link:hover {
  color: #fff;
}

.nav-link.router-link-active {
  color: #fff;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 2px;
  background: #f5e6d3;
  border-radius: 1px;
}

.publish-btn {
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 18px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.publish-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main {
  min-height: calc(100vh - 140px);
  padding: 30px 0;
}

.footer {
  background: #5d4e37;
  color: rgba(255, 255, 255, 0.7);
  padding: 20px 0;
  text-align: center;
  font-size: 14px;
}

@media (max-width: 768px) {
  .header {
    padding: 12px 0;
  }

  .header-content {
    flex-direction: column;
    gap: 12px;
  }

  .logo {
    gap: 8px;
  }

  .logo-icon {
    font-size: 24px;
  }

  .logo-text {
    font-size: 18px;
  }

  .nav {
    gap: 20px;
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }

  .nav-link {
    font-size: 14px;
  }

  .publish-btn {
    padding: 6px 14px;
    font-size: 13px;
  }

  .main {
    padding: 20px 0;
    min-height: calc(100vh - 160px);
  }

  .footer {
    padding: 16px 0;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .nav {
    gap: 16px;
  }

  .nav-link {
    font-size: 13px;
  }

  .publish-btn {
    padding: 5px 12px;
    font-size: 12px;
  }

  .logo-text {
    font-size: 16px;
  }

  .nav-link.router-link-active::after {
    bottom: -4px;
    height: 2px;
  }
}
</style>
