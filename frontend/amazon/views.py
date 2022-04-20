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
        'categories': Category.objects.all().order_by('-category'),
        'products': Product.objects.all(),
        'curr_nav': 'all'
    }
    return render(request, 'amazon/categories.html', context)

def categories(request, a_category):
    cat = Category.objects.get(category=a_category)
    products = Product.objects.filter(category_id=cat.id)
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': products,
        'curr_nav': a_category
    }
    return render(request, 'amazon/categories.html', context)

def oneProduct(request, a_product):
    product = Product.objects.get(name=a_product)
    curr_cat = product.category.category
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': product,
        'curr_nav': curr_cat
    }
    return render(request, 'amazon/product.html', context)