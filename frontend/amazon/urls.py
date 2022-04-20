from django.urls import path
from . import views
#from .views import ProductListView

urlpatterns = [
    path('', views.home, name='amazon-home'),
    path('about/', views.about, name='amazon-about'),
    path('all_products/', views.allProducts, name='amazon-products'),
    path('categories/<str:a_category>', views.categories, name='amazon-products-in-category'),
    path('product/<str:a_product>', views.oneProduct, name='amazon-one-product'),
    path('orders/', views.orders, name='amazon-orders'),
]