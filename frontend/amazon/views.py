from django.shortcuts import render
from django.http import HttpResponse

products = [
    {
        'name': 'apple',
        'price': '13',
        'seller': 'Erica'
    },
    {
        'name': 'pen',
        'price': '300',
        'seller': 'Beta'
    }
]

def home(request):
    context = {
        'products': products
    }
    return render(request, 'amazon/home.html', context)

def about(request):
    return render(request, 'amazon/about.html', {'title': 'About'})