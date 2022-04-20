from django.urls import path
from . import views
#from .views import ProductListView

urlpatterns = [
    path('', views.home, name='amazon-home'),
    path('about/', views.about, name='amazon-about'),
    path('all_products/', views.allProducts, name='amazon-products'),
    path('products/<str:a_category>', views.categories, name='amazon-products-in-category'),
]