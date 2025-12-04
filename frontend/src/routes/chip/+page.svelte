<script lang="ts">
	import { onMount } from 'svelte';
	import {
		Button,
		Tile,
		Loading,
		InlineNotification,
		DataTable,
		CopyButton
	} from 'carbon-components-svelte';
	import { Chip as ChipIcon, Renew } from 'carbon-icons-svelte';
	import { chipApi } from '$lib/api/client';
	import { deviceStore } from '$lib/stores/device';
	import type { ChipInfo } from '$lib/api/types';

	let chipInfo = $state<ChipInfo | null>(null);
	let loading = $state(false);
	let error = $state<string | null>(null);

	onMount(async () => {
		// Ensure device is selected
		if (!deviceStore.selectedDevice) {
			await deviceStore.loadDevices();
		}

		// Load chip info
		await loadChipInfo();
	});

	async function loadChipInfo() {
		loading = true;
		error = null;

		try {
			chipInfo = await chipApi.getInfo();
		} catch (err) {
			error = err instanceof Error ? err.message : '获取芯片信息失败';
			console.error('Failed to load chip info:', err);
		} finally {
			loading = false;
		}
	}

	function formatBytes(bytes: number): string {
		return (bytes / 1024).toFixed(2) + ' KB';
	}

	function copyToClipboard(text: string) {
		navigator.clipboard.writeText(text);
	}

	// Prepare data table rows for euiccInfo2
	let euiccInfo2Rows = $derived(
		chipInfo
			? [
					{ id: '1', property: 'Profile Version', value: chipInfo.euiccInfo2.profileVersion },
					{
						id: '2',
						property: 'SGP22 Version',
						value: chipInfo.euiccInfo2.svn
					},
					{
						id: '3',
						property: 'Firmware Version',
						value: chipInfo.euiccInfo2.euiccFirmwareVer
					},
					{
						id: '4',
						property: 'Global Platform Version',
						value: chipInfo.euiccInfo2.globalplatformVersion
					},
					{
						id: '5',
						property: 'UICC Firmware Version',
						value: chipInfo.euiccInfo2.uiccFirmwareVer
					},
					{
						id: '6',
						property: 'EUM Signed',
						value: chipInfo.euiccInfo2.extCardResource.freeNonVolatileMemory ? 'Yes' : 'No'
					}
				]
			: []
	);

	let resourceRows = $derived(
		chipInfo
			? [
					{
						id: '1',
						property: 'Installed Applications',
						value: chipInfo.euiccInfo2.extCardResource.installedApp.toString()
					},
					{
						id: '2',
						property: 'Free Non-Volatile Memory',
						value: formatBytes(chipInfo.euiccInfo2.extCardResource.freeNonVolatileMemory)
					},
					{
						id: '3',
						property: 'Free Volatile Memory',
						value: formatBytes(chipInfo.euiccInfo2.extCardResource.freeVolatileMemory)
					}
				]
			: []
	);

	const infoHeaders = [
		{ key: 'property', value: '属性', empty: false },
		{ key: 'value', value: '值', empty: false }
	];
</script>

<div class="chip-page">
	<div class="header">
		<div class="header-title">
			<ChipIcon size={32} />
			<div>
				<h1>芯片详细信息</h1>
				<p>查看 eUICC 芯片的详细技术信息</p>
			</div>
		</div>
		<Button on:click={loadChipInfo} disabled={loading} icon={Renew}>刷新</Button>
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
	{:else if loading}
		<div class="loading-container">
			<Loading description="正在加载芯片信息..." />
		</div>
	{:else if error}
		<InlineNotification
			kind="error"
			title="加载失败"
			subtitle={error}
			on:close={() => (error = null)}
		/>
		<Button on:click={loadChipInfo}>重试</Button>
	{:else if chipInfo}
		<div class="chip-info-container">
			<!-- EID Section -->
			<Tile class="eid-section">
				<h2>EID (eUICC 标识符)</h2>
				<div class="eid-display">
					<code class="eid-value">{chipInfo.eid}</code>
					<CopyButton text={chipInfo.eid} on:click={() => chipInfo && copyToClipboard(chipInfo.eid)} />
				</div>
				<p class="eid-description">
					EID 是 eUICC 的唯一标识符，类似于物理 SIM 卡的 ICCID。某些运营商可能需要此信息来激活服务。
				</p>
			</Tile>

			<!-- euiccInfo2 Section -->
			<Tile>
				<h2>eUICC 信息</h2>
				<DataTable headers={infoHeaders} rows={euiccInfo2Rows} size="compact">
					<svelte:fragment slot="cell" let:cell>
						<span class="table-cell">{cell.value}</span>
					</svelte:fragment>
				</DataTable>
			</Tile>

			<!-- Card Resources Section -->
			<Tile>
				<h2>卡资源</h2>
				<DataTable headers={infoHeaders} rows={resourceRows} size="compact">
					<svelte:fragment slot="cell" let:cell>
						<span class="table-cell">{cell.value}</span>
					</svelte:fragment>
				</DataTable>
			</Tile>

			<!-- Technical Details Section -->
			{#if chipInfo.euiccInfo2}
				<Tile>
					<h2>技术详情</h2>
					<div class="technical-details">
						<div class="detail-group">
							<h3>规范版本</h3>
							<div class="detail-item">
								<span class="detail-label">Profile Version:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.profileVersion}</span>
							</div>
							<div class="detail-item">
								<span class="detail-label">SGP.22 Version:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.svn}</span>
							</div>
							<div class="detail-item">
								<span class="detail-label">GlobalPlatform Version:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.globalplatformVersion}</span>
							</div>
						</div>

						<div class="detail-group">
							<h3>固件版本</h3>
							<div class="detail-item">
								<span class="detail-label">eUICC Firmware:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.euiccFirmwareVer}</span>
							</div>
							<div class="detail-item">
								<span class="detail-label">UICC Firmware:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.uiccFirmwareVer}</span>
							</div>
						</div>

						<div class="detail-group">
							<h3>类别和功能</h3>
							<div class="detail-item">
								<span class="detail-label">eUICC Category:</span>
								<span class="detail-value">{chipInfo.euiccInfo2.euiccCategory || 'N/A'}</span>
							</div>
							<div class="detail-item">
								<span class="detail-label">Forbidden Profile Policy Rules:</span>
								<span class="detail-value"
									>{chipInfo.euiccInfo2.forbiddenProfilePolicyRules || 'None'}</span
								>
							</div>
						</div>
					</div>
				</Tile>
			{/if}
		</div>
	{/if}
</div>

<style>
	.chip-page {
		max-width: 1200px;
		margin: 0 auto;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 2rem;
	}

	.header-title {
		display: flex;
		align-items: flex-start;
		gap: 1rem;
	}

	.header-title h1 {
		font-size: 2rem;
		font-weight: 300;
		margin-bottom: 0.5rem;
	}

	.header-title p {
		color: var(--cds-text-02);
	}

	.loading-container {
		display: flex;
		justify-content: center;
		padding: 3rem;
	}

	.chip-info-container {
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
	}

	:global(.eid-section) {
		background: linear-gradient(135deg, var(--cds-ui-01) 0%, var(--cds-ui-02) 100%);
	}

	:global(.eid-section) h2 {
		font-size: 1.25rem;
		font-weight: 400;
		margin-bottom: 1rem;
	}

	.eid-display {
		display: flex;
		align-items: center;
		gap: 1rem;
		margin-bottom: 1rem;
	}

	.eid-value {
		font-size: 1.25rem;
		font-family: 'IBM Plex Mono', monospace;
		background: var(--cds-ui-03);
		padding: 0.75rem 1rem;
		border-radius: 4px;
		flex: 1;
		letter-spacing: 0.05em;
	}

	.eid-description {
		font-size: 0.875rem;
		color: var(--cds-text-02);
		line-height: 1.5;
	}

	h2 {
		font-size: 1.25rem;
		font-weight: 400;
		margin-bottom: 1rem;
	}

	.table-cell {
		font-family: 'IBM Plex Mono', monospace;
		font-size: 0.875rem;
	}

	.technical-details {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
		gap: 2rem;
	}

	.detail-group h3 {
		font-size: 1rem;
		font-weight: 400;
		margin-bottom: 0.75rem;
		color: var(--cds-text-02);
	}

	.detail-item {
		display: flex;
		flex-direction: column;
		gap: 0.25rem;
		margin-bottom: 0.75rem;
	}

	.detail-label {
		font-size: 0.75rem;
		color: var(--cds-text-02);
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}

	.detail-value {
		font-family: 'IBM Plex Mono', monospace;
		font-size: 0.875rem;
	}
</style>
