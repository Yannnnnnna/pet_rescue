
import http from './request';

/** 
 * 微信小程序流式请求封装 (解决中文乱码问题) 
 * @param {String} url 接口地址 
 * @param {Object} data 请求参数 
 * @param {Function} onMessage 收到消息回调 (text, isDone) 
 */ 
export const streamRequest = (url, data, onMessage) => { 
    const baseURL = http.defaults.baseURL;
    const fullUrl = url.startsWith('http') ? url : baseURL + url;

    const requestTask = uni.request({ 
        url: fullUrl, 
        method: 'POST', 
        data: data, 
        enableChunked: true, // 🔥 关键：开启分块接收 
        responseType: 'arraybuffer', // 🔥 关键：接收二进制 
        header: {
            'content-type': 'application/json',
            'satoken': uni.getStorageSync('token') // 添加鉴权 token
        },
        success: (res) => { 
            // 请求完成 
            onMessage('', true); 
        }, 
        fail: (err) => { 
            console.error('AI请求失败', err); 
            onMessage('【网络错误】', true); 
        } 
    }); 

    // 解决中文乱码的核心：TextDecoder 
    // 注意：微信基础库 2.11.0+ 支持 TextDecoder，低版本可能需要 polyfill 
    const decoder = new TextDecoder('utf-8', { fatal: false }); 
    let buffer = '';

    requestTask.onChunkReceived((res) => { 
        const arrayBuffer = res.data; 
        // 解码二进制流 
        const chunk = decoder.decode(arrayBuffer, { stream: true });
        
        if (chunk) {
            buffer += chunk;
            
            // 检查是否有换行符，SSE 消息以 \n\n 分隔，但有时可能只是 \n
            // 我们需要逐行处理 data: 开头的内容
            const lines = buffer.split(/\r?\n/);
            
            // 保留最后一个可能未完成的片段
            buffer = lines.pop(); 
            
            for (let line of lines) {
                // 不要 trim()，否则会丢失缩进和空格
                // line = line.trim();
                if (line === '') continue; // 忽略空行（SSE的心跳或事件分隔符）
                
                if (line.startsWith('data:')) {
                    // 提取内容
                    let content = line.substring(5);
                    // 根据SSE规范，如果 data: 后紧跟一个空格，则去除该空格
                    // 但仅去除一个空格，保留后续的空格（如缩进）
                    if (content.startsWith(' ')) {
                        content = content.substring(1);
                    }
                    
                    // 如果是 [DONE] 则忽略或结束
                    if (content.trim() === '[DONE]') {
                        continue;
                    }

                    // 打印原始数据（调试用）
                    // console.log('SSE收到原始数据:', content);

                    // 尝试解析 JSON
                    try {
                        const json = JSON.parse(content);
                        if (json && typeof json.content === 'string') {
                            onMessage(json.content, false);
                        } else {
                            // 如果不是预期的JSON格式，尝试直接回调（兼容旧格式）
                             onMessage(content, false);
                        }
                    } catch (e) {
                        // 解析失败，说明不是JSON，直接回调原始内容
                        onMessage(content, false);
                    }
                }
            }
        }
    }); 

    return requestTask; // 返回 task 用于中断请求 
} 
