import request from '@/utils/request'

/**
 * 小程序微信一键登录
 * @param {string} code wx.login 返回的临时登录凭证
 * @param {Object} config 可选配置，支持 asForm=true 以 x-www-form-urlencoded 方式提交
 */
export const miniLogin = (code, config = {}) => {
  if (!code) {
    return Promise.reject(new Error('code is required'))
  }

  const { asForm, headers = {}, ...rest } = config
  const isForm = !!asForm

  const data = isForm
    ? `code=${encodeURIComponent(code)}`
    : { code }

  return request.post(
    '/sys-user/mini/login',
    data,
    {
      header: {
        'Content-Type': isForm
          ? 'application/x-www-form-urlencoded'
          : 'application/json',
        ...headers
      },
      ...rest
    }
  )
}

/**
 * 手机号密码登录
 * @param {Object} data 登录信息
 * @param {string} data.phone 手机号
 * @param {string} data.password 密码
 */
export const phoneLogin = (data) => {
  return request.post('/sys-user/phoneLogin', data)
}

/**
 * 退出登录
 */
export const logout = () => {
  return request.post('/sys-user/logout')
}

export default {
  miniLogin,
  phoneLogin,
  logout
}
