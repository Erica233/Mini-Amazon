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

def categories(request, a_category):
    cat = Category.objects.get(category=a_category)
    products = Product.objects.filter(category_id=cat.id)
    context = {
        'categories': Category.objects.all(),
        'products': products
    }
    return render(request, 'amazon/products.html', context)

