<script lang="ts">
	import { ToastNotification } from 'carbon-components-svelte';
	import { onMount } from 'svelte';

	interface ToastProps {
		kind?: 'error' | 'info' | 'info-square' | 'success' | 'warning' | 'warning-alt';
		title: string;
		subtitle?: string;
		caption?: string;
		timeout?: number;
		onClose?: () => void;
	}

	let {
		kind = 'info',
		title,
		subtitle = '',
		caption = '',
		timeout = 5000,
		onClose = () => {},
	}: ToastProps = $props();

	let visible = $state(true);

	onMount(() => {
		if (timeout > 0) {
			setTimeout(() => {
				visible = false;
				onClose();
			}, timeout);
		}
	});

	function handleClose() {
		visible = false;
		onClose();
	}
</script>

{#if visible}
	<div class="toast-container">
		<ToastNotification {kind} {title} {subtitle} {caption} on:close={handleClose} />
	</div>
{/if}

<style>
	.toast-container {
		position: fixed;
		top: 70px;
		right: 20px;
		z-index: 9999;
		max-width: 400px;
	}
</style>
