/**
 * 将文件转换为Base64格式
 * @param file - 要转换的文件
 * @returns Promise<string> - Base64格式的URL
 */
export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      const base64String = reader.result as string;
      // 提取Base64编码部分（去掉data:*/*;base64,前缀）
      const base64Data = base64String.split(',')[1];

      // 根据文件类型添加相应的前缀
      let dataUrl: string;
      const fileType = file.type.toLowerCase();

      if (fileType.includes('png')) {
        dataUrl = `data:image/png;base64,${base64Data}`;
      } else if (fileType.includes('jpeg') || fileType.includes('jpg')) {
        dataUrl = `data:image/jpeg;base64,${base64Data}`;
      } else if (fileType.includes('webp')) {
        dataUrl = `data:image/webp;base64,${base64Data}`;
      } else if (fileType.includes('gif')) {
        dataUrl = `data:image/gif;base64,${base64Data}`;
      } else {
        // 对于其他图片格式，使用通用格式
        dataUrl = `data:${file.type};base64,${base64Data}`;
      }

      resolve(dataUrl);
    };

    reader.onerror = () => {
      reject(new Error('Failed to convert file to base64'));
    };

    reader.readAsDataURL(file);
  });
}

/**
 * 将多个文件转换为Base64格式
 * @param files - 要转换的文件数组
 * @returns Promise<string[]> - Base64格式URL数组
 */
export async function filesToBase64(files: File[]): Promise<string[]> {
  const promises = files.map(file => fileToBase64(file));
  return Promise.all(promises);
}

/**
 * 获取文件的MIME类型
 * @param file - 文件对象
 * @returns string - MIME类型
 */
export function getFileMimeType(file: File): string {
  return file.type || 'application/octet-stream';
}

/**
 * 检查文件是否为图片
 * @param file - 文件对象
 * @returns boolean - 是否为图片
 */
export function isImageFile(file: File): boolean {
  return file.type.startsWith('image/');
}

/**
 * 获取图片格式的data URL前缀
 * @param mimeType - MIME类型
 * @returns string - data URL前缀
 */
export function getImageDataUrlPrefix(mimeType: string): string {
  const type = mimeType.toLowerCase();
  if (type.includes('png')) return 'data:image/png;base64,';
  if (type.includes('jpeg') || type.includes('jpg')) return 'data:image/jpeg;base64,';
  if (type.includes('webp')) return 'data:image/webp;base64,';
  if (type.includes('gif')) return 'data:image/gif;base64,';
  return `data:${mimeType};base64,`;
}