<script setup>
import CoffeeCompare from './components/CoffeeCompare.vue'
import Toast from './components/Toast.vue'
</script>

<template>
  <CoffeeCompare />
  <Toast />
</template>

<style scoped>
header {
  line-height: 1.5;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }
  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
