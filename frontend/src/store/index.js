import { createStore } from 'vuex'
import Cookies from 'js-cookie'

const store = createStore({
  state: {
    token: Cookies.get('token') || '',
    user: null,
    userRole: Cookies.get('userRole') || '',
    userId: Cookies.get('userId') ? parseInt(Cookies.get('userId')) : null
  },
  
  getters: {
    token: state => state.token,
    user: state => state.user,
    userRole: state => state.userRole,
    userId: state => state.userId,
    isLoggedIn: state => !!state.token
  },
  
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set('token', token, { expires: 7 })
    },
    
    SET_USER(state, user) {
      state.user = user
    },
    
    SET_USER_ROLE(state, role) {
      state.userRole = role
      Cookies.set('userRole', role, { expires: 7 })
    },
    
    SET_USER_ID(state, userId) {
      state.userId = userId
      Cookies.set('userId', userId.toString(), { expires: 7 })
    },
    
    CLEAR_AUTH(state) {
      state.token = ''
      state.user = null
      state.userRole = ''
      state.userId = null
      Cookies.remove('token')
      Cookies.remove('userRole')
      Cookies.remove('userId')
    }
  },
  
  actions: {
    login({ commit }, { token, user, role, userId }) {
      commit('SET_TOKEN', token)
      commit('SET_USER', user)
      commit('SET_USER_ROLE', role)
      // 确保设置用户ID
      if (userId) {
        commit('SET_USER_ID', userId)
      } else if (user && user.id) {
        commit('SET_USER_ID', user.id)
      }
    },
    
    logout({ commit }) {
      commit('CLEAR_AUTH')
    },
    
    updateUser({ commit }, user) {
      commit('SET_USER', user)
    }
  }
})

export default store
