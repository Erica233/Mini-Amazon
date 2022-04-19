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

def products(request, a_category):
    if a_category:
        products = Product.object.filter(category_category=a_category)
    else:
        products = Product.object.all()
    context = {
        'categories': Category.objects.all(),
        'products': products
    }
    return render(request, 'amazon/products.html', context)

