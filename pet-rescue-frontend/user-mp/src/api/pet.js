import request from '@/utils/request'

// 查询我的收藏列表
export const getMyFavorites = () => {
    return request.get('/pet-favorite/my')
}

// 查询我发布的宠物
export const getMyPublishedPets = () => {
    return request.post('/pet-info/myPets')
}

// 查询我沟通过的宠物
export const getMyChattedPets = () => {
    return request.get('/pet-info/myChattedPets')
}

// 查询我领养的宠物
export const getMyAdoptedPets = () => {
    return request.get('/pet-info/my-adopted-pets')
}

// 获取宠物详情
export const getPetDetail = (id) => {
    return request.get(`/pet-info/${id}`)
}

// 修改宠物信息
export const updatePet = (data) => {
    return request.post('/pet-info/update', data)
}

// 删除宠物
export const deletePet = (id) => {
    return request.delete(`/pet-info/delete/${id}`)
}
