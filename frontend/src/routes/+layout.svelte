<script lang="ts">
	import favicon from '$lib/assets/favicon.svg';
	import '../app.css';
	import 'carbon-components-svelte/css/g100.css'; // 使用 Carbon 暗色主题
	import Navbar from '$lib/components/Navbar.svelte';
	import ProgressBar from '$lib/components/ProgressBar.svelte';
	import { progressStore } from '$lib/stores/progress';
	import { onMount } from 'svelte';

	let { children } = $props();

	// 初始化 WebSocket 进度监听
	onMount(() => {
		progressStore.init();

		return () => {
			progressStore.destroy();
		};
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>MiniLPA WebUI</title>
</svelte:head>

<div class="app">
	<Navbar />
	<main class="content">
		{@render children()}
	</main>
	<ProgressBar />
</div>

<style>
	.app {
		min-height: 100vh;
		background: var(--cds-ui-background);
	}

	.content {
		padding: 2rem;
		max-width: 1400px;
		margin: 0 auto;
	}
</style>
