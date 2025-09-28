/**
 * 将DOM元素内容导出为HTML文件
 * @param element - 要导出的DOM元素
 * @param filename - 导出的文件名
 * @param title - HTML标题
 */
export async function exportElementToHtml(element: HTMLElement, filename: string = 'chat-export.html', title: string = '聊天记录'): Promise<void> {
  try {
    // 克隆元素以避免修改原始DOM
    const clonedElement = element.cloneNode(true) as HTMLElement;

    // 处理图片转换
    await processImagesInElement(clonedElement);

    // 获取所有样式
    const styles = getAllDocumentStyles();

    // 创建完整的HTML文档
    const htmlContent = createHtmlDocument(clonedElement.innerHTML, styles, title);

    // 创建并下载文件
    downloadHtmlFile(htmlContent, filename);
  } catch (error) {
    console.error('导出HTML失败:', error);
    throw new Error('导出HTML文件时发生错误');
  }
}

/**
 * 处理元素中的所有图片，将非base64图片转换为base64格式
 * @param element - 要处理的DOM元素
 */
async function processImagesInElement(element: HTMLElement): Promise<void> {
  const images = Array.from(element.querySelectorAll('img'));

  for (const img of images) {
    const src = img.getAttribute('src');
    if (!src) continue;

    // 检查是否已经是base64格式
    if (isBase64Image(src)) {
      continue;
    }

    try {
      // 将图片转换为base64
      const base64Src = await convertImageToBase64(src);
      img.setAttribute('src', base64Src);
    } catch (error) {
      console.warn('图片转换失败:', src, error);
      // 如果转换失败，保留原始src
    }
  }
}

/**
 * 检查图片URL是否为base64格式
 * @param src - 图片URL
 * @returns boolean - 是否为base64格式
 */
function isBase64Image(src: string): boolean {
  return src.startsWith('data:image/') && src.includes('base64');
}

/**
 * 将图片URL转换为base64格式
 * @param src - 图片URL
 * @returns Promise<string> - base64格式的图片URL
 */
async function convertImageToBase64(src: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image();

    // 处理跨域问题
    img.crossOrigin = 'anonymous';

    img.onload = () => {
      try {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) {
          reject(new Error('无法获取canvas上下文'));
          return;
        }

        canvas.width = img.width;
        canvas.height = img.height;

        // 绘制图片到canvas
        ctx.drawImage(img, 0, 0);

        // 获取base64数据
        const base64 = canvas.toDataURL('image/png');
        resolve(base64);
      } catch (error) {
        reject(error);
      }
    };

    img.onerror = () => {
      reject(new Error('图片加载失败'));
    };

    img.src = src;
  });
}

/**
 * 获取文档中的所有样式
 * @returns string - 所有样式的CSS字符串
 */
function getAllDocumentStyles(): string {
  let styles = '';

  // 获取所有样式表
  const styleSheets = document.styleSheets;

  for (let i = 0; i < styleSheets.length; i++) {
    const styleSheet = styleSheets[i];

    try {
      // 获取样式表中的所有规则
      const rules = styleSheet.cssRules || styleSheet.rules;

      for (let j = 0; j < rules.length; j++) {
        const rule = rules[j];
        styles += rule.cssText + '\n';
      }
    } catch (error) {
      // 如果无法访问样式表（可能是跨域问题），跳过
      console.warn('无法访问样式表:', styleSheet.href, error);
    }
  }

  return styles;
}

/**
 * 创建完整的HTML文档
 * @param content - HTML内容
 * @param styles - CSS样式
 * @param title - HTML标题
 * @returns string - 完整的HTML文档
 */
function createHtmlDocument(content: string, styles: string, title: string = '聊天记录'): string {
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .chat-container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            padding: 20px;
        }
        .message {
            margin-bottom: 15px;
            padding: 10px;
            border-radius: 6px;
        }
        .user-message {
            background-color: #e3f2fd;
            margin-left: 20px;
        }
        .assistant-message {
            background-color: #f5f5f5;
            margin-right: 20px;
        }
        .message-content {
            word-wrap: break-word;
        }
        .message-image {
            max-width: 100%;
            height: auto;
            border-radius: 4px;
            margin-top: 8px;
        }
        pre {
            background-color: #f4f4f4;
            padding: 10px;
            border-radius: 4px;
            overflow-x: auto;
        }
        code {
            background-color: #f4f4f4;
            padding: 2px 4px;
            border-radius: 2px;
            font-family: 'Courier New', monospace;
        }
        /* 导入原始样式 */
        ${styles}
    </style>
</head>
<body>
    <div class="chat-container">
        ${content}
    </div>
</body>
</html>`;
}

/**
 * 下载HTML文件
 * @param content - HTML内容
 * @param filename - 文件名
 */
function downloadHtmlFile(content: string, filename: string): void {
  const blob = new Blob([content], { type: 'text/html;charset=utf-8' });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.download = filename;

  // 触发下载
  document.body.appendChild(link);
  link.click();

  // 清理
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * 生成导出文件名
 * @param prefix - 文件名前缀
 * @returns string - 格式化的文件名
 */
export function generateExportFilename(prefix: string = 'chat'): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const timestamp = `${year}-${month}-${day}`;
  return `${prefix} ${timestamp}.html`;
}
