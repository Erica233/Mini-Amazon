from django.shortcuts import render
from django.http import HttpResponse
from .models import Item, Product, Category
#from django.views.generic import ListView

def home(request):
    context = {
        'categories': Category.objects.all()
    }
    return render(request, 'amazon/home.html', context)

def about(request):
    return render(request, 'amazon/about.html', {'title': 'About'})

def allProducts(request):
    context = {
        'categories': Category.objects.all(),
        'products': Product.objects.all()
    }
    return render(request, 'amazon/products.html', context)

def products(request, a_category):
    products = Product.objects.filter(product_category_category='Groceries')
    context = {
        'categories': Category.objects.all(),
        'products': products
    }
    return render(request, 'amazon/products.html', context)

