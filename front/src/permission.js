import router from './router'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import getPageTitle from '@/utils/get-page-title'
import { saveCurrentRoute } from './router'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()
  // set page title
  document.title = getPageTitle(to.meta.title)
  if (to.path === '/') {
    next({ path: '/dashboard' })
  } else {
    next()
  }
})

router.afterEach((to) => {
  // finish progress bar
  saveCurrentRoute(to)
  NProgress.done()
})

