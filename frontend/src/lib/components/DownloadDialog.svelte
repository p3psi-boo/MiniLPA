<script lang="ts">
	import {
		ComposedModal,
		ModalHeader,
		ModalBody,
		ModalFooter,
		Button,
		TextInput,
		FileUploaderDropContainer,
		InlineNotification,
		Tabs,
		Tab,
		TabContent
	} from 'carbon-components-svelte';
	import { Download, QrCode } from 'carbon-icons-svelte';
	import { profilesStore } from '$lib/stores/profiles';
	import { qrcodeApi } from '$lib/api/client';
	import type { DownloadRequest } from '$lib/api/types';

	interface DownloadDialogProps {
		open: boolean;
		onClose: () => void;
	}

	let { open = $bindable(), onClose }: DownloadDialogProps = $props();

	let selectedTab = $state(0);
	let activationCode = $state('');
	let confirmationCode = $state('');
	let imei = $state('');
	let uploadError = $state<string | null>(null);
	let parseError = $state<string | null>(null);
	let isUploading = $state(false);
	let isDownloading = $state(false);

	function resetForm() {
		activationCode = '';
		confirmationCode = '';
		imei = '';
		uploadError = null;
		parseError = null;
		selectedTab = 0;
	}

	function handleClose() {
		if (!isDownloading && !isUploading) {
			resetForm();
			onClose();
		}
	}

	async function handleDownload() {
		if (!activationCode.trim()) {
			parseError = '请输入激活码';
			return;
		}

		isDownloading = true;
		parseError = null;

		try {
			const downloadRequest: DownloadRequest = {
				smdp: '',
				matchingId: '',
				confirmationCode: confirmationCode.trim() || undefined,
				imei: imei.trim() || undefined
			};

			// Parse activation code (format: LPA:1$SMDP_ADDRESS$MATCHING_ID)
			const parts = activationCode.trim().split('$');
			if (parts.length >= 3) {
				downloadRequest.smdp = parts[1];
				downloadRequest.matchingId = parts[2];
			} else {
				throw new Error('激活码格式不正确');
			}

			await profilesStore.downloadProfile(downloadRequest);
			handleClose();
		} catch (error) {
			parseError = error instanceof Error ? error.message : '下载失败';
		} finally {
			isDownloading = false;
		}
	}

	async function handleFileUpload(event: CustomEvent<readonly File[]>) {
		const files = event.detail;
		if (files.length === 0) return;

		const file = files[0];
		if (!file.type.startsWith('image/')) {
			uploadError = '请上传图片文件';
			return;
		}

		isUploading = true;
		uploadError = null;
		parseError = null;

		try {
			const result = await qrcodeApi.parse(file);

			// Fill in the parsed values
			activationCode = `LPA:1$${result.smdp}$${result.matchingId || ''}`;
			if (result.confirmationCode) {
				confirmationCode = result.confirmationCode;
			}
			if (result.imei) {
				imei = result.imei;
			}

			// Switch to manual tab to show parsed values
			selectedTab = 0;
		} catch (error) {
			uploadError = error instanceof Error ? error.message : '二维码解析失败';
		} finally {
			isUploading = false;
		}
	}
</script>

<ComposedModal bind:open on:close={handleClose}>
	<ModalHeader label="下载配置文件" title="添加新的 eSIM 配置文件" />

	<ModalBody>
		{#if parseError}
			<InlineNotification
				kind="error"
				title="错误"
				subtitle={parseError}
				on:close={() => (parseError = null)}
			/>
		{/if}

		<Tabs bind:selected={selectedTab}>
			<Tab label="手动输入" />
			<Tab label="扫描二维码" />

			<svelte:fragment slot="content">
				<TabContent>
					<div class="form-section">
						<TextInput
							labelText="激活码 *"
							placeholder="LPA:1$SMDP_ADDRESS$MATCHING_ID"
							bind:value={activationCode}
							disabled={isDownloading || isUploading}
							required
						/>
						<TextInput
							labelText="确认码（可选）"
							placeholder="输入确认码（如果需要）"
							bind:value={confirmationCode}
							disabled={isDownloading || isUploading}
						/>
						<TextInput
							labelText="IMEI（可选）"
							placeholder="输入设备 IMEI"
							bind:value={imei}
							disabled={isDownloading || isUploading}
						/>
					</div>
				</TabContent>

				<TabContent>
					<div class="upload-section">
						{#if uploadError}
							<InlineNotification
								kind="error"
								title="上传失败"
								subtitle={uploadError}
								on:close={() => (uploadError = null)}
							/>
						{/if}

						<FileUploaderDropContainer
							labelText="拖拽二维码图片到此处或点击上传"
							accept={['image/*']}
							on:change={handleFileUpload}
							disabled={isUploading || isDownloading}
						/>

						{#if isUploading}
							<p class="upload-status">正在解析二维码...</p>
						{/if}
					</div>
				</TabContent>
			</svelte:fragment>
		</Tabs>
	</ModalBody>

	<ModalFooter>
		<Button kind="secondary" on:click={handleClose} disabled={isDownloading || isUploading}>
			取消
		</Button>
		<Button
			on:click={handleDownload}
			disabled={isDownloading || isUploading || !activationCode.trim()}
			icon={Download}
		>
			{isDownloading ? '下载中...' : '下载配置文件'}
		</Button>
	</ModalFooter>
</ComposedModal>

<style>
	.form-section {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding: 1rem 0;
	}

	.upload-section {
		padding: 1rem 0;
	}

	.upload-status {
		margin-top: 1rem;
		text-align: center;
		color: var(--cds-text-02);
	}
</style>
