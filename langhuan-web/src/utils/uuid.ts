/**
 * 生成UUID
 * @returns string 返回生成的UUID
 */
export function generateUUID(): string {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
            const r = (Math.random() * 16) | 0;
            const v = c === "x" ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        }
    );
}

/**
 * 生成简短UUID
 * @returns string 返回生成的简短UUID(不含横线)
 */
export function generateShortUUID(): string {
    return generateUUID().replace(/-/g, "");
}
