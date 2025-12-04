<script lang="ts">
	import {
		Tile,
		Button,
		Toggle,
		OverflowMenu,
		OverflowMenuItem,
		Modal,
		TextInput,
		InlineNotification
	} from 'carbon-components-svelte';
	import { CheckmarkFilled, WarningAlt, Edit, TrashCan } from 'carbon-icons-svelte';
	import type { Profile } from '$lib/api/types';
	import { profilesStore } from '$lib/stores/profiles';

	interface ProfileCardProps {
		profile: Profile;
	}

	let { profile }: ProfileCardProps = $props();

	let isEditing = $state(false);
	let isDeleting = $state(false);
	let editNickname = $state('');
	let operationError = $state<string | null>(null);
	let isOperating = $state(false);

	function handleEditClick() {
		editNickname = profile.nickname || profile.profileName;
		isEditing = true;
	}

	async function handleSaveNickname() {
		if (!editNickname.trim()) {
			operationError = '昵称不能为空';
			return;
		}

		isOperating = true;
		operationError = null;

		try {
			await profilesStore.setNickname(profile.iccid, editNickname.trim());
			isEditing = false;
		} catch (error) {
			operationError = error instanceof Error ? error.message : '设置昵称失败';
		} finally {
			isOperating = false;
		}
	}

	async function handleDelete() {
		isOperating = true;
		operationError = null;

		try {
			await profilesStore.deleteProfile(profile.iccid);
			isDeleting = false;
		} catch (error) {
			operationError = error instanceof Error ? error.message : '删除失败';
		} finally {
			isOperating = false;
		}
	}

	async function handleToggle() {
		const shouldEnable = profile.state === 'disabled';

		isOperating = true;
		operationError = null;

		try {
			if (shouldEnable) {
				await profilesStore.enableProfile(profile.iccid);
			} else {
				await profilesStore.disableProfile(profile.iccid);
			}
		} catch (error) {
			operationError = error instanceof Error ? error.message : '操作失败';
		} finally {
			isOperating = false;
		}
	}

	function getProfileClass(state: string): string {
		return state === 'enabled' ? 'profile-enabled' : '';
	}
</script>

<Tile class="profile-card {getProfileClass(profile.state)}">
	<div class="profile-header">
		<div class="profile-status">
			{#if profile.state === 'enabled'}
				<CheckmarkFilled size={24} class="status-icon enabled" />
			{:else}
				<WarningAlt size={24} class="status-icon disabled" />
			{/if}
			<div class="profile-title">
				<h3>{profile.nickname || profile.profileName}</h3>
				{#if profile.nickname && profile.nickname !== profile.profileName}
					<p class="profile-name-subtitle">{profile.profileName}</p>
				{/if}
			</div>
		</div>
		<div class="profile-actions">
			<Toggle
				size="sm"
				toggled={profile.state === 'enabled'}
				on:toggle={handleToggle}
				disabled={isOperating}
			/>
			<OverflowMenu flipped>
				<OverflowMenuItem text="编辑昵称" on:click={handleEditClick} />
				<OverflowMenuItem danger text="删除" on:click={() => (isDeleting = true)} />
			</OverflowMenu>
		</div>
	</div>

	{#if operationError}
		<InlineNotification
			kind="error"
			title="操作失败"
			subtitle={operationError}
			on:close={() => (operationError = null)}
		/>
	{/if}

	<div class="profile-info">
		<div class="info-row">
			<span class="label">ICCID:</span>
			<span class="value">{profile.iccid}</span>
		</div>
		<div class="info-row">
			<span class="label">提供商:</span>
			<span class="value">{profile.serviceProviderName}</span>
		</div>
		<div class="info-row">
			<span class="label">状态:</span>
			<span class="value status-{profile.state}"
				>{profile.state === 'enabled' ? '已启用' : '已禁用'}</span
			>
		</div>
		{#if profile.profileClass}
			<div class="info-row">
				<span class="label">类型:</span>
				<span class="value">{profile.profileClass}</span>
			</div>
		{/if}
	</div>
</Tile>

<!-- Edit Nickname Modal -->
<Modal
	bind:open={isEditing}
	modalHeading="编辑昵称"
	primaryButtonText="保存"
	secondaryButtonText="取消"
	on:click:button--secondary={() => (isEditing = false)}
	on:submit={handleSaveNickname}
	primaryButtonDisabled={isOperating}
>
	<TextInput
		labelText="配置文件昵称"
		placeholder="输入配置文件昵称"
		bind:value={editNickname}
		disabled={isOperating}
	/>
</Modal>

<!-- Delete Confirmation Modal -->
<Modal
	bind:open={isDeleting}
	danger
	modalHeading="确认删除"
	primaryButtonText="删除"
	secondaryButtonText="取消"
	on:click:button--secondary={() => (isDeleting = false)}
	on:submit={handleDelete}
	primaryButtonDisabled={isOperating}
>
	<p>确定要删除配置文件 <strong>{profile.nickname || profile.profileName}</strong> 吗？</p>
	<p>此操作无法撤销。</p>
</Modal>

<style>
	:global(.profile-card) {
		margin-bottom: 1rem;
	}

	:global(.profile-card.profile-enabled) {
		border-left: 4px solid var(--cds-support-02);
	}

	.profile-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 1rem;
	}

	.profile-status {
		display: flex;
		align-items: flex-start;
		gap: 0.75rem;
	}

	:global(.status-icon.enabled) {
		color: var(--cds-support-02);
	}

	:global(.status-icon.disabled) {
		color: var(--cds-text-03);
	}

	.profile-title h3 {
		font-size: 1.125rem;
		font-weight: 400;
		margin: 0;
	}

	.profile-name-subtitle {
		font-size: 0.875rem;
		color: var(--cds-text-02);
		margin: 0.25rem 0 0 0;
	}

	.profile-actions {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}

	.profile-info {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.info-row {
		display: flex;
		gap: 0.5rem;
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

	.status-enabled {
		color: var(--cds-support-02);
		font-weight: 500;
	}

	.status-disabled {
		color: var(--cds-text-03);
	}
</style>
