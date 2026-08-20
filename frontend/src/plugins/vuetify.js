// 「此刻」MD3 (Material Design 3) 主题配置 —— 珊瑚暖红种子色
// 色板基于 Material 3 参考实现生成 (seed: #E5393F)
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

const m3Light = {
  dark: false,
  colors: {
    background: '#FBFAF9',
    surface: '#FBFAF9',
    'surface-bright': '#FFFFFF',
    'surface-light': '#FFF8F7',
    'surface-variant': '#F5DEDA',
    'on-surface': '#201A19',
    'on-surface-variant': '#52443D',
    'on-surface-light': '#574440',
    primary: '#B3261E',
    'primary-darken-1': '#8F1D16',
    'primary-container': '#FFDAD6',
    'on-primary': '#FFFFFF',
    'on-primary-container': '#410002',
    secondary: '#775652',
    'secondary-container': '#FFDAD6',
    'on-secondary': '#FFFFFF',
    'on-secondary-container': '#2C1512',
    tertiary: '#745B47',
    'tertiary-container': '#FFDCC2',
    'on-tertiary': '#FFFFFF',
    'on-tertiary-container': '#291806',
    error: '#BA1A1A',
    'error-container': '#FFDAD6',
    'on-error': '#FFFFFF',
    'on-error-container': '#410002',
    success: '#2E7D32',
    'success-container': '#B7F0B1',
    'on-success': '#FFFFFF',
    warning: '#8C5000',
    'warning-container': '#FFDCC2',
    info: '#00639B',
    'info-container': '#CBE6FF',
    outline: '#85736C',
    'outline-variant': '#D8C2BB'
  },
  variables: {
    'border-color': '#DAD2CF',
    'border-opacity': 1,
    'high-emphasis-opacity': 0.87,
    'medium-emphasis-opacity': 0.6,
    'disabled-opacity': 0.38,
    'idle-opacity': 0.55,
    'hover-opacity': 0.06,
    'focus-opacity': 0.1,
    'selected-opacity': 0.1,
    'activated-opacity': 0.12,
    'pressed-opacity': 0.12,
    'dragged-opacity': 0.08,
    'theme-kbd': '#F5DFDA',
    'theme-on-kbd': '#201A19',
    'theme-code': '#F5DFDA',
    'theme-on-code': '#201A19'
  }
}

const m3Dark = {
  dark: true,
  colors: {
    background: '#1A1412',
    surface: '#1A1412',
    'surface-bright': '#3D3734',
    'surface-light': '#231C1A',
    'surface-variant': '#52443D',
    'on-surface': '#F0DEDA',
    'on-surface-variant': '#D8C2BB',
    primary: '#FFB4AB',
    'primary-darken-1': '#FF8A80',
    'primary-container': '#93000A',
    'on-primary': '#690005',
    'on-primary-container': '#FFDAD6',
    secondary: '#E7BDB6',
    'secondary-container': '#5D3F3B',
    'on-secondary': '#3E2723',
    'on-secondary-container': '#FFDAD6',
    tertiary: '#F0BE9B',
    'tertiary-container': '#4A3220',
    'on-tertiary': '#3E2A1B',
    'on-tertiary-container': '#FFDCC2',
    error: '#FFB4AB',
    'error-container': '#93000A',
    'on-error': '#690005',
    'on-error-container': '#FFDAD6',
    success: '#7BD88F',
    'success-container': '#195F33',
    on_success: '#003919',
    warning: '#FFB864',
    'warning-container': '#6A4600',
    info: '#A5CFFF',
    'info-container': '#004B6E',
    outline: '#A28D86',
    'outline-variant': '#52443D'
  },
  variables: {
    'border-color': '#52443D',
    'border-opacity': 1
  }
}

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'm3Light',
    themes: { m3Light, m3Dark }
  },
  defaults: {
    VBtn: { rounded: 'lg', variant: 'tonal', font: true },
    VTextField: { variant: 'outlined', rounded: 'lg', density: 'comfortable' },
    VCard: { rounded: 'xl' },
    VDialog: { rounded: 'xl' },
    VChip: { rounded: 'pill' },
    VNavigationDrawer: { border: 'end' }
  }
})
