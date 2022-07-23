<template>
  <div>
    <hero />
    <hr />
    <home-content />
  </div>
</template>

<script>
import HomeContent from "../components/HomeContent";

export default {
  name: "home",
  components: { 
    HomeContent,
  },
};
</script>

<style lang="css" scoped>
.next-steps .fa-link {
    margin-right: 5px;
}
</style>
