import request from '@/utils/request'

/**
 * 获取我的个人信息
 */
export const getMyInfo = () => {
    return request.get('/sys-user/my/info')
}

/**
 * 修改个人信息 (昵称-头像-手机)
 * @param {Object} data - 用户信息
 * @param {string} data.nickname - 昵称
 * @param {string} data.avatar - 头像URL
 * @param {string} data.phone - 手机号
 */
export const updateUserInfo = (data) => {
    return request.post('/sys-user/update/info', data)
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 旧密码
 * @param {string} data.newPassword - 新密码
 */
export const updatePassword = (data) => {
    return request.post('/sys-user/update/password', data)
}

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @returns {Promise<string>} 图片URL
 */
export const uploadImage = (filePath) => {
    return new Promise((resolve, reject) => {
        uni.uploadFile({
            url: request.defaults.baseURL + '/file/upload',
            filePath: filePath,
            name: 'file',
            header: {
                'satoken': uni.getStorageSync('token')
            },
            success: (res) => {
                try {
                    const data = JSON.parse(res.data)
                    if (data.code === 200 || data.code === 0) {
                        resolve(data.data) // 返回图片URL
                    } else {
                        uni.showToast({
                            title: data.msg || '上传失败',
                            icon: 'none'
                        })
                        reject(new Error(data.msg || '上传失败'))
                    }
                } catch (error) {
                    reject(error)
                }
            },
            fail: (error) => {
                uni.showToast({
                    title: '上传失败',
                    icon: 'none'
                })
                reject(error)
            }
        })
    })
}

/**
 * 获取用户详情
 * @param {string} id - 用户ID
 */
export const getUserDetail = (id) => {
    return request.get(`/sys-user/detail/${id}`)
}

export default {
    getMyInfo,
    updateUserInfo,
    updatePassword,
    uploadImage,
    getUserDetail
}
