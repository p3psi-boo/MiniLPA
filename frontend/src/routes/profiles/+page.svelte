<script lang="ts">
	import { onMount } from 'svelte';
	import { Button, Loading, InlineNotification } from 'carbon-components-svelte';
	import { Add } from 'carbon-icons-svelte';
	import { profilesStore } from '$lib/stores/profiles';
	import { deviceStore } from '$lib/stores/device';
	import ProfileCard from '$lib/components/ProfileCard.svelte';
	import DownloadDialog from '$lib/components/DownloadDialog.svelte';
	import type { Profile } from '$lib/api/types';

	let showDownloadDialog = $state(false);

	onMount(async () => {
		// Ensure device is selected
		if (!deviceStore.selectedDevice) {
			await deviceStore.loadDevices();
		}

		// Load profiles
		await profilesStore.loadProfiles();
	});

	function handleRefresh() {
		profilesStore.loadProfiles();
	}
</script>

<div class="profiles-page">
	<div class="header">
		<div>
			<h1>配置文件管理</h1>
			<p>管理您的 eSIM 配置文件</p>
		</div>
		<div class="header-actions">
			<Button kind="secondary" on:click={handleRefresh} disabled={profilesStore.loading}>
				刷新
			</Button>
			<Button icon={Add} on:click={() => (showDownloadDialog = true)}>下载新配置文件</Button>
		</div>
	</div>

	{#if !deviceStore.selectedDevice}
		<InlineNotification
			kind="warning"
			title="未选择设备"
			subtitle="请先返回首页选择一个读卡器设备"
		>
			<svelte:fragment slot="actions">
				<Button href="/" kind="tertiary">返回首页</Button>
			</svelte:fragment>
		</InlineNotification>
	{:else if profilesStore.loading}
		<div class="loading-container">
			<Loading description="正在加载配置文件..." />
		</div>
	{:else if profilesStore.error}
		<InlineNotification
			kind="error"
			title="加载失败"
			subtitle={profilesStore.error}
			on:close={() => profilesStore.clearError()}
		/>
		<Button on:click={handleRefresh}>重试</Button>
	{:else if profilesStore.profiles.length === 0}
		<div class="empty-state">
			<InlineNotification
				kind="info"
				title="暂无配置文件"
				subtitle="您的设备上还没有任何 eSIM 配置文件。点击下方按钮添加新的配置文件。"
			/>
			<Button icon={Add} on:click={() => (showDownloadDialog = true)}>下载新配置文件</Button>
		</div>
	{:else}
		<div class="profiles-container">
			<div class="profiles-stats">
				<div class="stat">
					<span class="stat-label">总计:</span>
					<span class="stat-value">{profilesStore.profiles.length}</span>
				</div>
				<div class="stat">
					<span class="stat-label">已启用:</span>
					<span class="stat-value enabled">
						{profilesStore.profiles.filter((p: Profile) => p.state === 'enabled').length}
					</span>
				</div>
				<div class="stat">
					<span class="stat-label">已禁用:</span>
					<span class="stat-value disabled">
						{profilesStore.profiles.filter((p: Profile) => p.state === 'disabled').length}
					</span>
				</div>
			</div>

			<div class="profiles-list">
				{#each profilesStore.profiles as profile (profile.iccid)}
					<ProfileCard {profile} />
				{/each}
			</div>
		</div>
	{/if}
</div>

<DownloadDialog bind:open={showDownloadDialog} onClose={() => (showDownloadDialog = false)} />

<style>
	.profiles-page {
		max-width: 1200px;
		margin: 0 auto;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem;
	}

	.header h1 {
		font-size: 2rem;
		font-weight: 300;
		margin-bottom: 0.5rem;
	}

	.header p {
		color: var(--cds-text-02);
	}

	.header-actions {
		display: flex;
		gap: 0.5rem;
	}

	.loading-container {
		display: flex;
		justify-content: center;
		padding: 3rem;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 2rem;
		padding: 3rem;
	}

	.profiles-container {
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
	}

	.profiles-stats {
		display: flex;
		gap: 2rem;
		padding: 1rem;
		background: var(--cds-ui-01);
		border-radius: 4px;
	}

	.stat {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.stat-label {
		font-size: 0.875rem;
		color: var(--cds-text-02);
	}

	.stat-value {
		font-size: 1.5rem;
		font-weight: 300;
		font-family: 'IBM Plex Mono', monospace;
	}

	.stat-value.enabled {
		color: var(--cds-support-02);
	}

	.stat-value.disabled {
		color: var(--cds-text-03);
	}

	.profiles-list {
		display: flex;
		flex-direction: column;
	}
</style>
