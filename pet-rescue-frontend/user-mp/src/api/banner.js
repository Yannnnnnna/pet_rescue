import request from '@/utils/request'

export const getBannerList = () => {
    return request.get('/sys-banner/show')
}
