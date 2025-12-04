/**
 * 设备状态管理
 * 使用 Svelte 5 Runes
 */

import type { Device } from '../api/types';
import { deviceApi } from '../api/client';

class DeviceStore {
	devices = $state<Device[]>([]);
	selectedDevice = $state<Device | null>(null);
	loading = $state(false);
	error = $state<string | null>(null);

	/**
	 * 加载设备列表
	 */
	async loadDevices() {
		this.loading = true;
		this.error = null;
		try {
			const response = await deviceApi.list();
			this.devices = response.devices;

			// 如果只有一个设备，自动选择
			if (this.devices.length === 1) {
				this.selectedDevice = this.devices[0];
			}
		} catch (err) {
			this.error = err instanceof Error ? err.message : '加载设备列表失败';
			console.error('Failed to load devices:', err);
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 选择设备
	 */
	selectDevice(device: Device) {
		this.selectedDevice = device;
	}

	/**
	 * 清除错误
	 */
	clearError() {
		this.error = null;
	}
}

export const deviceStore = new DeviceStore();
