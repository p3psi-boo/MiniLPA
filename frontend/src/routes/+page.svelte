<script lang="ts">
	import { onMount } from 'svelte';
	import { Button, Tile, Loading, InlineNotification } from 'carbon-components-svelte';
	import { Chip, ArrowRight } from 'carbon-icons-svelte';
	import { deviceStore } from '$lib/stores/device';
	import { chipApi } from '$lib/api/client';
	import type { ChipInfo } from '$lib/api/types';

	let chipInfo = $state<ChipInfo | null>(null);
	let loadingChipInfo = $state(false);
	let chipError = $state<string | null>(null);

	// 页面加载时获取设备列表
	onMount(async () => {
		await deviceStore.loadDevices();

		// 如果有选中的设备，加载芯片信息
		if (deviceStore.selectedDevice) {
			await loadChipInfo();
		}
	});

	// 加载芯片信息
	async function loadChipInfo() {
		loadingChipInfo = true;
		chipError = null;
		try {
			chipInfo = await chipApi.getInfo();
		} catch (err) {
			chipError = err instanceof Error ? err.message : '获取芯片信息失败';
			console.error('Failed to load chip info:', err);
		} finally {
			loadingChipInfo = false;
		}
	}

	// 选择设备
	async function handleDeviceSelect(device: typeof deviceStore.devices[0]) {
		deviceStore.selectDevice(device);
		await loadChipInfo();
	}
</script>

<div class="device-page">
	<div class="header">
		<h1>设备选择</h1>
		<p>请选择一个 eUICC 读卡器设备以继续</p>
	</div>

	{#if deviceStore.loading}
		<div class="loading-container">
			<Loading description="正在加载设备列表..." />
		</div>
	{:else if deviceStore.error}
		<InlineNotification
			kind="error"
			title="加载失败"
			subtitle={deviceStore.error}
			on:close={() => deviceStore.clearError()}
		/>
		<Button on:click={() => deviceStore.loadDevices()}>重试</Button>
	{:else if deviceStore.devices.length === 0}
		<InlineNotification
			kind="warning"
			title="未检测到设备"
			subtitle="请确保读卡器已连接并且驱动程序已正确安装"
		/>
		<Button on:click={() => deviceStore.loadDevices()}>刷新</Button>
	{:else}
		<div class="devices-grid">
			{#each deviceStore.devices as device}
				<Tile
					class="device-tile {deviceStore.selectedDevice?.path === device.path
						? 'selected'
						: ''}"
					on:click={() => handleDeviceSelect(device)}
				>
					<div class="device-info">
						<div class="device-icon">
							<Chip size={32} />
						</div>
						<div class="device-details">
							<h3>{device.name}</h3>
							<p class="device-path">{device.path}</p>
						</div>
					</div>
					{#if deviceStore.selectedDevice?.path === device.path}
						<div class="selected-indicator">
							<ArrowRight size={24} />
						</div>
					{/if}
				</Tile>
			{/each}
		</div>

		{#if deviceStore.selectedDevice}
			<div class="chip-info-section">
				<h2>芯片信息</h2>
				{#if loadingChipInfo}
					<Loading description="正在加载芯片信息..." />
				{:else if chipError}
					<InlineNotification
						kind="error"
						title="加载失败"
						subtitle={chipError}
						on:close={() => (chipError = null)}
					/>
				{:else if chipInfo}
					<Tile>
						<div class="chip-info-grid">
							<div class="info-item">
								<span class="label">EID:</span>
								<span class="value">{chipInfo.eid}</span>
							</div>
							<div class="info-item">
								<span class="label">配置文件版本:</span>
								<span class="value">{chipInfo.euiccInfo2.profileVersion}</span>
							</div>
							<div class="info-item">
								<span class="label">固件版本:</span>
								<span class="value">{chipInfo.euiccInfo2.euiccFirmwareVer}</span>
							</div>
							<div class="info-item">
								<span class="label">已安装应用:</span>
								<span class="value">{chipInfo.euiccInfo2.extCardResource.installedApp}</span>
							</div>
							<div class="info-item">
								<span class="label">可用内存 (非易失):</span>
								<span class="value"
									>{(chipInfo.euiccInfo2.extCardResource.freeNonVolatileMemory / 1024).toFixed(
										2
									)} KB</span
								>
							</div>
							<div class="info-item">
								<span class="label">可用内存 (易失):</span>
								<span class="value"
									>{(chipInfo.euiccInfo2.extCardResource.freeVolatileMemory / 1024).toFixed(2)} KB</span
								>
							</div>
						</div>
					</Tile>

					<div class="action-buttons">
						<Button href="/profiles">管理配置文件</Button>
						<Button kind="secondary" href="/notifications">查看通知</Button>
					</div>
				{/if}
			</div>
		{/if}
	{/if}
</div>

<style>
	.device-page {
		max-width: 1200px;
		margin: 0 auto;
	}

	.header {
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

	.loading-container {
		display: flex;
		justify-content: center;
		padding: 3rem;
	}

	.devices-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
		gap: 1rem;
		margin-bottom: 2rem;
	}

	:global(.device-tile) {
		cursor: pointer;
		transition: all 0.2s ease;
		position: relative;
	}

	:global(.device-tile:hover) {
		background: var(--cds-hover-ui);
	}

	:global(.device-tile.selected) {
		border: 2px solid var(--cds-interactive-01);
	}

	.device-info {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.device-icon {
		color: var(--cds-interactive-01);
	}

	.device-details h3 {
		font-size: 1.125rem;
		font-weight: 400;
		margin-bottom: 0.25rem;
	}

	.device-path {
		font-size: 0.875rem;
		color: var(--cds-text-02);
		font-family: 'IBM Plex Mono', monospace;
	}

	.selected-indicator {
		position: absolute;
		top: 1rem;
		right: 1rem;
		color: var(--cds-interactive-01);
	}

	.chip-info-section {
		margin-top: 3rem;
	}

	.chip-info-section h2 {
		font-size: 1.5rem;
		font-weight: 300;
		margin-bottom: 1rem;
	}

	.chip-info-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
		gap: 1.5rem;
	}

	.info-item {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
	}

	.label {
		font-size: 0.875rem;
		color: var(--cds-text-02);
	}

	.value {
		font-size: 1rem;
		font-family: 'IBM Plex Mono', monospace;
	}

	.action-buttons {
		display: flex;
		gap: 1rem;
		margin-top: 2rem;
	}
</style>
