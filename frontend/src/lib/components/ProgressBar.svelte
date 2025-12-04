<script lang="ts">
	import { ProgressBar as CarbonProgressBar } from 'carbon-components-svelte';
	import { progressStore } from '$lib/stores/progress';

	// 监听 progressStore 的变化
	$effect(() => {
		// 确保 progressStore 已初始化
		if (!progressStore) return;
	});
</script>

{#if progressStore.isOperating || progressStore.type === 'complete' || progressStore.type === 'error'}
	<div class="progress-container">
		<div class="progress-message">
			{#if progressStore.type === 'error'}
				<span class="error-icon">❌</span>
			{:else if progressStore.type === 'complete'}
				<span class="success-icon">✅</span>
			{:else}
				<span class="progress-icon">⏳</span>
			{/if}
			<span>{progressStore.message}</span>
		</div>
		{#if progressStore.type === 'progress'}
			<CarbonProgressBar value={progressStore.percent} max={100} />
			<div class="progress-percent">{progressStore.percent}%</div>
		{/if}
	</div>
{/if}

<style>
	.progress-container {
		position: fixed;
		bottom: 20px;
		right: 20px;
		background: var(--cds-ui-01);
		border: 1px solid var(--cds-ui-03);
		border-radius: 4px;
		padding: 16px;
		min-width: 300px;
		max-width: 400px;
		box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
		z-index: 9999;
	}

	.progress-message {
		display: flex;
		align-items: center;
		gap: 8px;
		margin-bottom: 12px;
		font-size: 14px;
	}

	.error-icon,
	.success-icon,
	.progress-icon {
		font-size: 18px;
	}

	.progress-percent {
		text-align: right;
		margin-top: 8px;
		font-size: 12px;
		color: var(--cds-text-02);
	}
</style>
