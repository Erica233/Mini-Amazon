from django.urls import path
from . import views
#from .views import ProductListView

urlpatterns = [
    path('', views.home, name='amazon-home'),
    path('about/', views.about, name='amazon-about'),
    path('all_products/', views.allProducts, name='amazon-products'),
    path('products/<int: a_category>', views.products, name='amazon-products'),
]