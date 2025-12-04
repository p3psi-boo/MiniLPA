<script lang="ts">
	import { onMount } from 'svelte';
	import {
		Button,
		Tile,
		Loading,
		InlineNotification,
		Modal,
		DataTable,
		Toolbar,
		ToolbarContent
	} from 'carbon-components-svelte';
	import { CheckmarkOutline, TrashCan, Renew } from 'carbon-icons-svelte';
	import { notificationsStore } from '$lib/stores/notifications';
	import { deviceStore } from '$lib/stores/device';
	import type { Notification } from '$lib/api/types';

	let selectedNotification = $state<Notification | null>(null);
	let showProcessModal = $state(false);
	let showDeleteModal = $state(false);
	let isOperating = $state(false);
	let operationError = $state<string | null>(null);

	onMount(async () => {
		// Ensure device is selected
		if (!deviceStore.selectedDevice) {
			await deviceStore.loadDevices();
		}

		// Load notifications
		await notificationsStore.loadNotifications();
	});

	function handleRefresh() {
		notificationsStore.loadNotifications();
	}

	function handleProcessClick(notification: Notification) {
		selectedNotification = notification;
		showProcessModal = true;
	}

	function handleDeleteClick(notification: Notification) {
		selectedNotification = notification;
		showDeleteModal = true;
	}

	async function handleProcess() {
		if (!selectedNotification) return;

		isOperating = true;
		operationError = null;

		try {
			await notificationsStore.processNotification(selectedNotification.seqNumber);
			showProcessModal = false;
			selectedNotification = null;
		} catch (error) {
			operationError = error instanceof Error ? error.message : '处理失败';
		} finally {
			isOperating = false;
		}
	}

	async function handleDelete() {
		if (!selectedNotification) return;

		isOperating = true;
		operationError = null;

		try {
			await notificationsStore.deleteNotification(selectedNotification.seqNumber);
			showDeleteModal = false;
			selectedNotification = null;
		} catch (error) {
			operationError = error instanceof Error ? error.message : '删除失败';
		} finally {
			isOperating = false;
		}
	}

	function getNotificationTypeName(type: string): string {
		const typeMap: Record<string, string> = {
			install: '安装',
			enable: '启用',
			disable: '禁用',
			delete: '删除'
		};
		return typeMap[type] || type;
	}

	const headers = [
		{ key: 'seqNumber', value: '序列号', empty: false },
		{ key: 'profileManagementOperation', value: '操作类型', empty: false },
		{ key: 'notificationAddress', value: '通知地址', empty: false },
		{ key: 'iccid', value: 'ICCID', empty: false },
		{ key: 'actions', value: '操作', empty: false }
	];

	// Transform notifications for DataTable
	let rows = $derived(
		notificationsStore.notifications.map((notification: Notification) => ({
			id: notification.seqNumber.toString(),
			seqNumber: notification.seqNumber,
			profileManagementOperation: getNotificationTypeName(notification.profileManagementOperation),
			notificationAddress: notification.notificationAddress,
			iccid: notification.iccid || 'N/A',
			notification: notification
		}))
	);
</script>

<div class="notifications-page">
	<div class="header">
		<div>
			<h1>通知管理</h1>
			<p>查看和处理来自 SM-DP+ 服务器的通知</p>
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
	{:else if notificationsStore.loading}
		<div class="loading-container">
			<Loading description="正在加载通知..." />
		</div>
	{:else if notificationsStore.error}
		<InlineNotification
			kind="error"
			title="加载失败"
			subtitle={notificationsStore.error}
			on:close={() => notificationsStore.clearError()}
		/>
		<Button on:click={handleRefresh}>重试</Button>
	{:else if notificationsStore.notifications.length === 0}
		<div class="empty-state">
			<InlineNotification
				kind="info"
				title="暂无通知"
				subtitle="当前没有待处理的通知。通知通常由运营商发送，用于确认配置文件的安装、启用、禁用或删除操作。"
			/>
			<Button on:click={handleRefresh} icon={Renew}>刷新</Button>
		</div>
	{:else}
		<div class="notifications-container">
			<DataTable {headers} {rows}>
				<Toolbar>
					<ToolbarContent>
						<Button on:click={handleRefresh} icon={Renew}>刷新</Button>
					</ToolbarContent>
				</Toolbar>

				<svelte:fragment slot="cell" let:row let:cell>
					{#if cell.key === 'actions'}
						<div class="action-buttons">
							<Button
								size="small"
								kind="tertiary"
								icon={CheckmarkOutline}
								on:click={() => handleProcessClick(row.notification)}
							>
								处理
							</Button>
							<Button
								size="small"
								kind="danger-tertiary"
								icon={TrashCan}
								on:click={() => handleDeleteClick(row.notification)}
							>
								删除
							</Button>
						</div>
					{:else}
						{cell.value}
					{/if}
				</svelte:fragment>
			</DataTable>
		</div>
	{/if}
</div>

<!-- Process Notification Modal -->
<Modal
	bind:open={showProcessModal}
	modalHeading="处理通知"
	primaryButtonText="处理"
	secondaryButtonText="取消"
	on:click:button--secondary={() => (showProcessModal = false)}
	on:submit={handleProcess}
	primaryButtonDisabled={isOperating}
>
	{#if operationError}
		<InlineNotification
			kind="error"
			title="操作失败"
			subtitle={operationError}
			on:close={() => (operationError = null)}
		/>
	{/if}

	{#if selectedNotification}
		<p>确定要处理此通知吗？</p>
		<div class="notification-details">
			<div class="detail-row">
				<span class="label">序列号:</span>
				<span class="value">{selectedNotification.seqNumber}</span>
			</div>
			<div class="detail-row">
				<span class="label">操作类型:</span>
				<span class="value"
					>{getNotificationTypeName(selectedNotification.profileManagementOperation)}</span
				>
			</div>
			<div class="detail-row">
				<span class="label">通知地址:</span>
				<span class="value">{selectedNotification.notificationAddress}</span>
			</div>
			{#if selectedNotification.iccid}
				<div class="detail-row">
					<span class="label">ICCID:</span>
					<span class="value">{selectedNotification.iccid}</span>
				</div>
			{/if}
		</div>
		<p class="info-text">处理通知将向 SM-DP+ 服务器发送确认消息。</p>
	{/if}
</Modal>

<!-- Delete Notification Modal -->
<Modal
	bind:open={showDeleteModal}
	danger
	modalHeading="删除通知"
	primaryButtonText="删除"
	secondaryButtonText="取消"
	on:click:button--secondary={() => (showDeleteModal = false)}
	on:submit={handleDelete}
	primaryButtonDisabled={isOperating}
>
	{#if operationError}
		<InlineNotification
			kind="error"
			title="操作失败"
			subtitle={operationError}
			on:close={() => (operationError = null)}
		/>
	{/if}

	{#if selectedNotification}
		<p>确定要删除此通知吗？</p>
		<p class="warning-text">删除通知不会向服务器发送任何消息，只是从本地移除记录。</p>
	{/if}
</Modal>

<style>
	.notifications-page {
		max-width: 1400px;
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

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 2rem;
		padding: 3rem;
	}

	.notifications-container {
		margin-top: 1rem;
	}

	.action-buttons {
		display: flex;
		gap: 0.5rem;
	}

	.notification-details {
		margin: 1.5rem 0;
		padding: 1rem;
		background: var(--cds-ui-01);
		border-radius: 4px;
	}

	.detail-row {
		display: flex;
		gap: 0.5rem;
		margin-bottom: 0.5rem;
	}

	.detail-row:last-child {
		margin-bottom: 0;
	}

	.label {
		font-size: 0.875rem;
		color: var(--cds-text-02);
		min-width: 100px;
	}

	.value {
		font-size: 0.875rem;
		font-family: 'IBM Plex Mono', monospace;
		word-break: break-all;
	}

	.info-text {
		margin-top: 1rem;
		font-size: 0.875rem;
		color: var(--cds-text-02);
	}

	.warning-text {
		margin-top: 0.5rem;
		font-size: 0.875rem;
		color: var(--cds-support-01);
	}
</style>
