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

def category(request):
    context = {
        'categories': Category.objects.all()
    }
    return render(request, 'amazon/category.html', context)

