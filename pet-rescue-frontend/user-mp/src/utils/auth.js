/**
 * 登录状态管理工具
 */

/**
 * 检查是否已登录
 * @returns {boolean}
 */
export const isLoggedIn = () => {
    const token = uni.getStorageSync('token')
    return !!token
}

/**
 * 获取token
 * @returns {string}
 */
export const getToken = () => {
    return uni.getStorageSync('token') || ''
}

/**
 * 保存token
 * @param {string} token
 */
export const setToken = (token) => {
    uni.setStorageSync('token', token)
}

/**
 * 清除token
 */
export const clearToken = () => {
    uni.removeStorageSync('token')
}

/**
 * 跳转到登录页
 * @param {string} redirect - 登录成功后跳转的页面
 */
export const toLogin = (redirect = '') => {
    clearToken()

    const url = redirect
        ? `/pages/login/login?redirect=${encodeURIComponent(redirect)}`
        : '/pages/login/login'

    uni.reLaunch({
        url
    })
}

/**
 * 检查登录状态，未登录则跳转到登录页
 * @param {string} redirect - 登录成功后跳转的页面
 * @returns {boolean} 是否已登录
 */
export const checkLogin = (redirect = '') => {
    if (!isLoggedIn()) {
        toLogin(redirect)
        return false
    }
    return true
}

export default {
    isLoggedIn,
    getToken,
    setToken,
    clearToken,
    toLogin,
    checkLogin
}
