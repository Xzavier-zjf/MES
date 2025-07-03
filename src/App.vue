<template>
  <router-view></router-view>

</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores'

const appStore = useAppStore()

onMounted(() => {
  // 初始化数据
  appStore.fetchAllData()
})
</script>

<style>
/* 可选样式 */
body {
  margin: 0;
  font-family: 'Segoe UI', sans-serif;
}
</style>
