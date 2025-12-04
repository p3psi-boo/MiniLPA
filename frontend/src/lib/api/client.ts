/**
 * API 客户端封装
 * 提供统一的 HTTP 请求接口
 */

import type {
	Device,
	ChipInfo,
	Profile,
	Notification,
	DownloadRequest,
	NicknameRequest,
	QRCodeParseResponse,
	ApiError,
} from './types';

const API_BASE = '/api';

/**
 * 通用的 fetch 封装
 */
async function request<T>(
	url: string,
	options?: RequestInit
): Promise<T> {
	try {
		const response = await fetch(url, {
			...options,
			headers: {
				'Content-Type': 'application/json',
				...options?.headers,
			},
		});

		if (!response.ok) {
			const error: ApiError = await response.json();
			throw new Error(error.error || `HTTP ${response.status}: ${response.statusText}`);
		}

		return await response.json();
	} catch (error) {
		if (error instanceof Error) {
			throw error;
		}
		throw new Error('Network request failed');
	}
}

/**
 * 设备管理 API
 */
export const deviceApi = {
	/**
	 * 获取设备列表
	 */
	async list(): Promise<{ devices: Device[] }> {
		return request<{ devices: Device[] }>(`${API_BASE}/devices`);
	},
};

/**
 * 芯片信息 API
 */
export const chipApi = {
	/**
	 * 获取芯片信息
	 */
	async getInfo(): Promise<ChipInfo> {
		return request<ChipInfo>(`${API_BASE}/chip/info`);
	},
};

/**
 * 配置文件管理 API
 */
export const profileApi = {
	/**
	 * 获取所有配置文件
	 */
	async list(): Promise<{ profiles: Profile[] }> {
		return request<{ profiles: Profile[] }>(`${API_BASE}/profiles`);
	},

	/**
	 * 下载新的配置文件（异步）
	 */
	async download(data: DownloadRequest): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/profiles`, {
			method: 'POST',
			body: JSON.stringify(data),
		});
	},

	/**
	 * 启用配置文件
	 */
	async enable(iccid: string): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/profiles/${iccid}/enable`, {
			method: 'PUT',
		});
	},

	/**
	 * 禁用配置文件
	 */
	async disable(iccid: string): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/profiles/${iccid}/disable`, {
			method: 'PUT',
		});
	},

	/**
	 * 设置配置文件昵称
	 */
	async setNickname(iccid: string, nickname: string): Promise<{ status: string }> {
		const data: NicknameRequest = { nickname };
		return request<{ status: string }>(`${API_BASE}/profiles/${iccid}/nickname`, {
			method: 'PUT',
			body: JSON.stringify(data),
		});
	},

	/**
	 * 删除配置文件
	 */
	async delete(iccid: string): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/profiles/${iccid}`, {
			method: 'DELETE',
		});
	},
};

/**
 * 通知管理 API
 */
export const notificationApi = {
	/**
	 * 获取所有通知
	 */
	async list(): Promise<{ notifications: Notification[] }> {
		return request<{ notifications: Notification[] }>(`${API_BASE}/notifications`);
	},

	/**
	 * 处理通知
	 */
	async process(seqNumber: number): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/notifications/${seqNumber}/process`, {
			method: 'POST',
		});
	},

	/**
	 * 删除通知
	 */
	async delete(seqNumber: number): Promise<{ status: string }> {
		return request<{ status: string }>(`${API_BASE}/notifications/${seqNumber}`, {
			method: 'DELETE',
		});
	},
};

/**
 * 二维码解析 API
 */
export const qrcodeApi = {
	/**
	 * 解析二维码图片
	 */
	async parse(imageFile: File): Promise<QRCodeParseResponse> {
		const formData = new FormData();
		formData.append('image', imageFile);

		const response = await fetch(`${API_BASE}/qrcode/parse`, {
			method: 'POST',
			body: formData,
		});

		if (!response.ok) {
			const error: ApiError = await response.json();
			throw new Error(error.error || '二维码解析失败');
		}

		return await response.json();
	},
};
