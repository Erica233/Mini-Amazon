import socket
from django.contrib import messages
from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.urls import reverse
from django.core.mail import send_mail

from .models import Item, Product, Category, Package, Warehouse


#from django.views.generic import ListView

def home(request):
    return render(request, 'amazon/home.html')

def allProducts(request):
    if request.method == "POST":
        search_products = request.POST.get('name', '')
        products = Product.objects.filter(name__icontains=search_products)
    else:
        products = Product.objects.all()
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': products,
        'curr_nav': 'all'
    }
    return render(request, 'amazon/categories.html', context)

def categories(request, a_category):
    try:
        cat = Category.objects.get(category=a_category)
    except Category.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This category does not exist!')
        return HttpResponseRedirect(reverse('amazon-products'))
    products = Product.objects.filter(category_id=cat.id)
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'products': products,
        'curr_nav': a_category
    }
    return render(request, 'amazon/categories.html', context)

@login_required
def oneProduct(request, a_product):
    try:
        product = Product.objects.get(name=a_product)
    except Product.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This product does not exist!')
        return HttpResponseRedirect(reverse('amazon-products'))
    curr_cat = product.category.category

    if request.method == "POST":
        product_num = request.POST['product_num']
        if Item.objects.filter(buyer=request.user, product=product, in_cart=True).exists():
            item = Item.objects.get(buyer=request.user, product=product, in_cart=True)
            item.product_num += int(product_num)
            item.save()
        else:
            Item.objects.create(buyer=request.user, product=product, product_num=product_num)
        messages.add_message(request, messages.INFO, 'Add to Shopping Cart Successfully!')
        return HttpResponseRedirect(request.META.get('HTTP_REFERER'))
    else:
        context = {
            'categories': Category.objects.all().order_by('-category'),
            'product': product,
            'curr_nav': curr_cat
        }
        return render(request, 'amazon/product.html', context)

@login_required
def cart(request):
    items = Item.objects.filter(buyer=request.user, in_cart=True).order_by('id')
    if request.method == "POST" and 'remove' in request.POST:
        item_id = request.POST['remove']
        item = Item.objects.get(id=item_id)
        item.delete()
        messages.add_message(request, messages.INFO, 'Removed ' + item.product.name + ' From Your Cart Successfully!')
        return HttpResponseRedirect(reverse('amazon-cart'))
    elif request.method == "POST" and '+' in request.POST:
        item_id = request.POST['+']
        item = Item.objects.get(id=item_id)
        item.product_num += 1
        item.save()
        return HttpResponseRedirect(reverse('amazon-cart'))
    elif request.method == "POST" and '-' in request.POST:
        item_id = request.POST['-']
        item = Item.objects.get(id=item_id)
        if item.product_num == 1:
            return HttpResponseRedirect(reverse('amazon-cart'))
        else:
            item.product_num -= 1
            item.save()
            return HttpResponseRedirect(reverse('amazon-cart'))
    else:
        total_price = 0
        for item in items:
            total_price += item.product_num * item.product.price
        context = {
            'items': items,
            'total_price': total_price
        }
        return render(request, 'amazon/cart.html', context)

@login_required
def checkout(request):
    items = Item.objects.filter(buyer=request.user, in_cart=True)
    if not items:
        messages.add_message(request, messages.ERROR, 'No Items in Your Cart! Shopping First!')
        return HttpResponseRedirect(reverse('amazon-products'))
    if request.method == "POST":
        package_price = 0
        destination_x = request.POST['destination_x']
        destination_y = request.POST['destination_y']
        ups_account = request.POST.get('ups_account', '')
        ups_verified = False
        # allocate the nearest warehouse location
        warehouses = Warehouse.objects.all()
        min_dist = 10000
        warehouse = Warehouse.objects.get(id=1)
        for wh in warehouses.iterator():
            dist = abs(wh.location_x - int(destination_x)) ** 2 + abs(wh.location_y - int(destination_y)) ** 2
            if min_dist > dist:
                min_dist = dist
                warehouse = wh
        package = Package.objects.create(owner=request.user, warehouse=warehouse, destination_x=destination_x,
                                         destination_y=destination_y, ups_account=ups_account, ups_verified=ups_verified,
                                         package_price=package_price)
        item_list = "" # used for email content
        for item in items:
            package_price += item.product_num * item.product.price
            item.in_cart = False
            item.package = package
            item.save()
            item_list += (item.product.name + " * " + str(item.product_num) + "\n")
        package.package_price = package_price
        package.save()
        # send email to the user to notify the success of making this order
        subject = 'Your order #' + str(package.id) + ' is confirmed - Mini-Amazon'
        msg = 'Dear ' + request.user.username + ', \nThanks for shopping at Mini-Amazon!\nWe have received your order' \
              ' and are dealing with it! Please be patient!\n\nOrder No.' + str(package.id) + ':\nCreated Time: ' + \
              str(package.create_time) + '\n\nItem list:\n' + item_list + '\nTotal price: $' + str(package_price) + \
              '\n\nThanks,\nMini-Amazon Group\n'
        send_mail(
            subject,
            msg,
            'gaozedong1111@gmail.com',
            [request.user.email],
            fail_silently=False,
        )
        messages.add_message(request, messages.INFO, 'Your Order is Placed Successfully!')
        return HttpResponseRedirect('amazon-one-order', package_id=package.id)
    else:
        context = {
            'items': items,
        }
        return render(request, 'amazon/checkout.html', context)

@login_required
def orders(request):
    items = Item.objects.filter(buyer=request.user)
    packages = Package.objects.filter(owner=request.user).order_by('-create_time')
    context = {
        'categories': Category.objects.all().order_by('-category'),
        'items': items,
        'packages': packages,
        'curr_nav': 'all'
    }
    return render(request, 'amazon/orders.html', context)

@login_required
def oneOrder(request, package_id):
    try:
        package = Package.objects.get(id=package_id)
    except Package.DoesNotExist:
        messages.add_message(request, messages.ERROR, 'This order does not exist!')
        return HttpResponseRedirect(reverse('amazon-orders'))
    if package.owner != request.user:
        messages.add_message(request, messages.ERROR, 'You do not have this order!')
        return HttpResponseRedirect(reverse('amazon-orders'))
    items = package.items.all()
    context = {
        'items': items,
        'package': package
    }
    return render(request, 'amazon/oneOrder.html', context)