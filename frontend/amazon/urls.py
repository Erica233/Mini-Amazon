from django.urls import path
from . import views
#from .views import ProductListView

urlpatterns = [
    path('', views.home, name='amazon-home'),
    path('all_products/', views.allProducts, name='amazon-products'),
    path('categories/<str:a_category>', views.categories, name='amazon-products-in-category'),
    path('product/<str:a_product>', views.oneProduct, name='amazon-one-product'),
    path('orders/', views.orders, name='amazon-orders'),
    path('orders/<int:package_id>', views.oneOrder, name='amazon-one-order'),
    path('cart/', views.cart, name='amazon-cart'),
    path('checkout/', views.checkout, name='amazon-checkout'),
]